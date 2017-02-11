package com.earldouglas.xwp

import java.util.concurrent.atomic.AtomicReference
import sbt._
import sbt.Keys._

object ContainerPlugin extends AutoPlugin {

  lazy val quickstart = taskKey[Seq[Process]]("quickstart container")
  lazy val start      = taskKey[Seq[Process]]("start container")
  lazy val debug      = taskKey[Seq[Process]]("start container in debug mode")
  lazy val join       = taskKey[Seq[Int]]("join container")
  lazy val stop       = taskKey[Unit]("stop container")

  object autoImport {
    val Container = config("container").hide

    val debugAddress            = settingKey[String]("address to be used for debugging")
    val debugOptions            = settingKey[String => Seq[String]]("debug options")

    val containerLibs           = settingKey[Seq[ModuleID]]("container libraries")
    val containerMain           = settingKey[String]("container main class")
    val containerPort           = settingKey[Int]("port number to be used by container")
    val containerConfigFile     = settingKey[Option[File]]("path of container configuration file")
    val containerArgs           = settingKey[Seq[String]]("additional container args")
    val containerForkOptions    = settingKey[ForkOptions]("fork options")
    val containerShutdownOnExit = settingKey[Boolean]("shutdown container on sbt exit")
    val containerScale          = settingKey[Int]("number of container instances to start")

    val containerLaunchCmd      = taskKey[Seq[String]]("command to launch container")
  }

  private lazy val containerInstances =
    settingKey[AtomicReference[Seq[Process]]]("current container process")

  import WebappPlugin.autoImport.webappPrepare
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
      , quickstart         := quickstartTask.value
      , debug              := (debugTask dependsOn webappPrepare).value
      , join               := joinTask.value
      , stop               := stopTask.value
      , onLoad in Global   := onLoadSetting.value
      , javaOptions        := (javaOptions in Compile).value
      , containerLaunchCmd <<= defaultLaunchCmd
      ))

  val _debugAddress: String =
    if (System.getProperty("os.name").toLowerCase.indexOf("win") >= 0) {
      "debug"
    } else {
      "8888"
    }

  def _debugOptions(address: String): Seq[String] =
    Seq( "-Xdebug"
       , Seq( "-Xrunjdwp:transport=dt_socket"
            , "address=" + address
            , "server=y"
            , "suspend=n"
            ).mkString(",")
       )

  lazy val baseContainerSettings =
    Seq( containerPort           := 8080
       , containerConfigFile     := None
       , containerArgs           := Nil
       , containerForkOptions    := new ForkOptions
       , containerInstances      := new AtomicReference(Seq.empty[Process])
       , containerShutdownOnExit := true
       , debugAddress            := _debugAddress
       , debugOptions            := _debugOptions
       , containerScale          := 1
       )

  private def defaultLaunchCmd = Def.task {
    val portArg: Seq[String] = Seq("--port", containerPort.value.toString)

    val configArg: Seq[String] = containerConfigFile.value match {
      case Some(file) => Seq("--config", file.absolutePath)
      case None => Nil
    }

    Seq(containerMain.value) ++
      portArg ++
      configArg ++
      containerArgs.value :+
      (target in webappPrepare).value.absolutePath
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

      containerLaunchCmd.value match {
        case Nil =>
          sys.error("no launch command specified")
        case launchCmd =>
          def launchFn(port: Int): Process = {
            val args: Seq[String] =
              javaOptions.value ++
              debugOptions.value(debugAddress.value).filter(_ => debug) ++
              launchCmd map { x =>
                if (quick && x == (target in webappPrepare).value.absolutePath) {
                  (sourceDirectory in webappPrepare).value.absolutePath
                } else if (x == containerPort.value.toString) {
                  port.toString
                } else {
                  x
                }
              }
            startup(log, libs, args, containerForkOptions.value)
          }

          val startPort: Int = containerPort.value
          val endPort: Int = containerPort.value + containerScale.value - 1

          val processes: Seq[Process] = (startPort to endPort) map launchFn
          instances.set(processes)
          processes
      }
    }

  private def joinTask: Def.Initialize[Task[Seq[Int]]] =
    Def.task { containerInstances.value.get map { _.exitValue } }

  private def stopTask: Def.Initialize[Task[Unit]] =
    Def.task { shutdown(streams.value.log, containerInstances.value) }

  private def validateSbtVerison(version: String): Unit = {
    val versionArray = version.split("\\.").map(_.toInt)
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
}
