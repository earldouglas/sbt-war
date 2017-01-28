package com.earldouglas.xwp

import java.util.concurrent.atomic.AtomicReference

import sbt._, Keys._

object ContainerPlugin extends AutoPlugin {

  lazy val quickstart = taskKey[Process]("quickstart container")
  lazy val start      = taskKey[Process]("start container")
  lazy val debug      = taskKey[Process]("start container in debug mode")
  lazy val join       = taskKey[Option[Int]]("join container")
  lazy val stop       = taskKey[Unit]("stop container")

  object autoImport {
    val Container = config("container").hide

    val debugOptions            = settingKey[Seq[String]]("debug options")

    val containerLibs           = settingKey[Seq[ModuleID]]("container libraries")
    val containerMain           = settingKey[String]("container main class")
    val containerPort           = settingKey[Int]("port number to be used by container")
    val containerConfigFile     = settingKey[Option[File]]("path of container configuration file")
    val containerArgs           = settingKey[Seq[String]]("additional container args")
    val containerForkOptions    = settingKey[ForkOptions]("fork options")
    val containerShutdownOnExit = settingKey[Boolean]("shutdown container on sbt exit")

    val containerLaunchCmd      = taskKey[Seq[String]]("command to launch container")
  }

  private lazy val containerInstance =
    settingKey[AtomicReference[Option[Process]]]("Current container process")

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

  lazy val baseContainerSettings = Seq(
    containerPort           := -1
  , containerConfigFile     := None
  , containerArgs           := Nil
  , containerForkOptions    := new ForkOptions
  , containerInstance       := new AtomicReference(Option.empty[Process])
  , containerShutdownOnExit := true
  , debugOptions            := Seq( "-Xdebug"
                                  , "-Xrunjdwp:transport=dt_socket,address=8888,server=y,suspend=n"
                                  )
  )

  private def defaultLaunchCmd = Def.task {
    val portArg: Seq[String] = containerPort.value match {
      case p if p > 0 => Seq("--port", p.toString)
      case _ => Nil
    }

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
      val instance = containerInstance.value

      shutdown(log, instance)

      val libs: Seq[File] =
        Seq( if (quick) (fullClasspath in Runtime).value.map(_.data) else Seq.empty
           , Classpaths.managedJars(conf, classpathTypes.value, update.value).map(_.data)
           ).flatten

      containerLaunchCmd.value match {
        case Nil =>
          sys.error("no launch command specified")
        case launchCmd =>
          val args: Seq[String] =
            javaOptions.value ++
            (if (debug) debugOptions.value else Seq.empty) ++
            launchCmd map { x =>
              if (quick && x == (target in webappPrepare).value.absolutePath) {
                (sourceDirectory in webappPrepare).value.absolutePath
              } else {
                x
              }
            }
          val process = startup(log, libs, args, containerForkOptions.value)
          instance.set(Option(process))
          process
      }
    }

  private def joinTask: Def.Initialize[Task[Option[Int]]] =
    Def.task { containerInstance.value.get map { _.exitValue } }

  private def stopTask: Def.Initialize[Task[Unit]] =
    Def.task { shutdown(streams.value.log, containerInstance.value) }

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
          state.addExitHook(shutdown(state.log, containerInstance.value))
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
                      , atomicRef: AtomicReference[Option[Process]]
                      ): Unit = {
    val oldProcess = atomicRef.getAndSet(None)
    oldProcess.foreach(stopProcess(l))
  }
}
