package com.earldouglas.xwp

import java.util.concurrent.atomic.AtomicReference
import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey
import sbt.Keys._
import Compat.Process

object ContainerPlugin extends AutoPlugin {

  lazy val quickstart = taskKey[Seq[Process]]("quickstart container")
  lazy val start      = taskKey[Seq[Process]]("start container")
  lazy val debug      = taskKey[Seq[Process]]("start container in debug mode")
  lazy val join       = taskKey[Seq[Int]]("join container")
  lazy val stop       = taskKey[Unit]("stop container")
  lazy val quicktest  = taskKey[Unit]("test under quickstarted container")
  lazy val test       = taskKey[Unit]("test under started container")

  object autoImport {
    val Container = config("container").hide

    val debugPort               = settingKey[Int]("port to be used for debugging")
    val debugOptions            = settingKey[Int => Seq[String]]("debug options")

    val containerLibs           = settingKey[Seq[ModuleID]]("container libraries")
    val containerMain           = settingKey[String]("container main class")
    val containerPort           = settingKey[Int]("port number to be used by container")
    val containerArgs           = settingKey[Seq[String]]("additional container args")
    val containerForkOptions    = settingKey[ForkOptions]("fork options")
    val containerShutdownOnExit = settingKey[Boolean]("shutdown container on sbt exit")
    val containerScale          = settingKey[Int]("number of container instances to start")

    val containerLaunchCmd      = taskKey[(Int, String) => Seq[String]]("command to launch container")
  }

  private lazy val containerInstances =
    settingKey[AtomicReference[Seq[Process]]]("current container process")

  import WebappPlugin.autoImport.webappPrepare
  import WebappPlugin.autoImport.webappPrepareQuick
  import autoImport._

  override def requires = WarPlugin

  override val projectConfigurations = Seq(Container)

  override lazy val projectSettings =
    containerSettings(Container) ++
      inConfig(Container)(Seq(
        containerLibs := Nil
      , containerMain := ""
      ))

  def containerSettings(conf: Configuration) =
    baseContainerSettings ++
      Seq(libraryDependencies ++= (containerLibs in conf).value.map(_ % conf)) ++
      inConfig(conf)(Seq(
        start              := (startTask dependsOn webappPrepare).value
      , quickstart         := (quickstartTask dependsOn webappPrepareQuick).value
      , debug              := (debugTask dependsOn webappPrepare).value
      , join               := joinTask.value
      , stop               := stopTask.value
      , quicktest          := ((test in Test) dependsOn awaitOpenTask dependsOn quickstart dependsOn (compile in Test)).value
      , test               := ((test in Test) dependsOn awaitOpenTask dependsOn start dependsOn (compile in Test)).value
      , onLoad in Global   := onLoadSetting.value
      , javaOptions        := (javaOptions in Compile).value
      , containerLaunchCmd := defaultLaunchCmd.value
      ))

  def _debugOptions(port: Int): Seq[String] =
    Seq( "-Xdebug"
       , Seq( "-Xrunjdwp:transport=dt_socket"
            , "address=" + port
            , "server=y"
            , "suspend=n"
            ).mkString(",")
       )

  lazy val baseContainerSettings =
    Seq( containerPort           := 8080
       , containerArgs           := Nil
       , containerForkOptions    := ForkOptions()
       , containerInstances      := new AtomicReference(Seq.empty[Process])
       , containerShutdownOnExit := true
       , debugPort               := 8888
       , debugOptions            := _debugOptions
       , containerScale          := 1
       )

  private def defaultLaunchCmd =
    Def.task {
      val main = containerMain.value
      val args = containerArgs.value

      { (port: Int, path: String) =>
        val portArg: Seq[String] = Seq("--port", port.toString)
        Seq(main) ++
          portArg ++
          args :+
          path
      }
    }

  private def startTask      = launchTask(false, false)
  private def debugTask      = launchTask(false, true)
  private def quickstartTask = launchTask(true, false)

  private def launchTask(quick: Boolean, debug: Boolean) =
    Def.task {
      val log = streams.value.log
      val conf = configuration.value
      val instances = containerInstances.value

      shutdown(log, instances)

      val libs: Seq[File] =
        (fullClasspath in Runtime).value.map(_.data).filter(_ => quick) ++
        Classpaths.managedJars(conf, classpathTypes.value, update.value).map(_.data)

      val path = {
        if (quick) {
          target in webappPrepareQuick
        } else {
          target in webappPrepare
        }
      }.value.absolutePath

      def launchFn(_containerPort: Int, _debugPort: Int): Process = {
        val args: Seq[String] =
          javaOptions.value ++
          debugOptions.value(_debugPort).filter(_ => debug) ++
          containerLaunchCmd.value(_containerPort, path)
        startup(log, libs, args, containerForkOptions.value)
      }

      val startContainerPort: Int = containerPort.value
      val endContainerPort: Int = containerPort.value + containerScale.value - 1

      val startDebugPort: Int = debugPort.value
      val endDebugPort: Int = debugPort.value + containerScale.value - 1

      val ports: Seq[(Int,Int)] =
        (startContainerPort to endContainerPort) zip
        (startDebugPort to endDebugPort)

      val processes: Seq[Process] = ports map { case (c, d) => launchFn(c, d) }
      instances.set(processes)
      processes
    }

  private def joinTask: Def.Initialize[Task[Seq[Int]]] =
    Def.task { containerInstances.value.get map { _.exitValue } }

  private def stopTask: Def.Initialize[Task[Unit]] =
    Def.task { shutdown(streams.value.log, containerInstances.value) }

  private def validateSbtVerison(version: String): Unit = {
    val versionArray = version.split("\\.").map(_.split("-")).map(_(0).toInt)
    val major = versionArray(0)
    val minor = versionArray(1)
    val patch = versionArray(2)

    if ((major == 0 && minor < 13) ||
        (major == 0 && minor == 13 && patch < 6)) {
      throw new RuntimeException(
         "xsbt-web-plugin requires sbt 0.13.6+, " +
         "but this project is configured to use sbt " +
         version
      )
    }
  }

  private def onLoadSetting: Def.Initialize[State => State] =
    Def.setting {
      (onLoad in Global).value compose { state: State =>
        validateSbtVerison(state.configuration.provider.id.version)
        if ((containerShutdownOnExit).value) {
          state.addExitHook(shutdown(state.log, containerInstances.value))
        } else {
          state
        }
      }
    }

  private def startup( l: Logger
                     , libs: Seq[File]
                     , args: Seq[String]
                     , forkOptions: ForkOptions
                     ): Process = {
    l.info("starting server ...")
    val cp = Path.makeString(libs)
    new Fork("java", None).fork(forkOptions, Seq("-cp", cp) ++ args)
  }

  private def stopProcess(l: Logger)(p: Process): Unit = {
    l.info("waiting for server to shut down...")
    p.destroy()
    val err = System.err
    val devNull: java.io.PrintStream =
      new java.io.PrintStream(
        new java.io.OutputStream {
          def write(b: Int): Unit = {}
        }
      )
    System.setErr(devNull)
    p.exitValue()
    System.setErr(err)
  }

  private def shutdown( l: Logger
                      , atomicRef: AtomicReference[Seq[Process]]
                      ): Unit = {
    val oldProcess = atomicRef.getAndSet(Seq.empty[Process])
    oldProcess.foreach(stopProcess(l))
  }

  private def awaitOpenTask: Def.Initialize[Task[Unit]] = {
    def awaitOpen(port: Int, retries: Int = (30 * 4)): Unit = {
      def isOpen(port: Int): Boolean =
        try {
          import java.net.Socket
          import java.net.InetSocketAddress
          val socket = new Socket()
          socket.connect(new InetSocketAddress("localhost", port))
          socket.close()
          true
        } catch {
          case e: Exception => false
        }
      if (!isOpen(port)) {
        if (retries > 0) {
          Thread.sleep(250)
          awaitOpen(port, retries - 1)
        } else {
          throw new Exception(s"gave up waiting for port $port to be open")
        }
      }
    }
    Def.task {
      val port = containerPort.value
      awaitOpen(port)
    }
  }
}
