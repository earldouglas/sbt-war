package com.earldouglas.xwp

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import sbt._, Keys._

object ContainerPlugin extends AutoPlugin {

  lazy val start = taskKey[Process]("start container")
  lazy val stop  = taskKey[Unit]("stop container")

  object autoImport {
    val Container = config("container").hide

    val containerLibs        = settingKey[Seq[ModuleID]]("container libraries")
    val containerMain        = settingKey[String]("container main class")
    val containerPort        = settingKey[Int]("port number to be used by container")
    val containerConfigFile  = settingKey[Option[File]]("path of container configuration file")
    val containerArgs        = settingKey[Seq[String]]("additional container args")
    val containerLaunchCmd   = taskKey[Seq[String]]("command to launch container")
    val containerForkOptions = taskKey[ForkOptions]("fork options")
  }

  private lazy val containerInstance = settingKey[AtomicReference[Option[Process]]]("Current container process")

  import WebappPlugin.autoImport.webappPrepare
  import autoImport._

  override def requires = WarPlugin

  override def trigger = allRequirements

  override val projectConfigurations = Seq(Container)

  override lazy val projectSettings =
    containerSettings(Container) ++
      inConfig(Container)(Seq(
        containerLibs      := Nil
      , containerMain      := ""
      , containerLaunchCmd := Nil
      ))

  def containerSettings(conf: Configuration) = {
    val atomicRef = new AtomicReference(Option.empty[Process])

    baseContainerSettings ++
      Seq(libraryDependencies ++= (containerLibs in conf).value.map(_ % conf)) ++
      inConfig(conf)(Seq(
        start            <<= startTask dependsOn webappPrepare
      , stop             <<= stopTask
      , onLoad in Global <<= onLoadSetting
      , javaOptions      <<= javaOptions in Compile
      ))
  }

  lazy val baseContainerSettings = Seq(
    containerPort        := -1
  , containerConfigFile  := None
  , containerArgs        := Nil
  , containerForkOptions := new ForkOptions
  , containerInstance    := new AtomicReference(Option.empty[Process])
  )


  private def startTask = Def.task {
    val log = streams.value.log
    val conf = configuration.value
    val instance = containerInstance.value

    shutdown(log, instance)

    val libs: Seq[File] =
      Classpaths.managedJars(conf, classpathTypes.value, update.value).map(_.data)

    containerLaunchCmd.value match {
      case Nil =>
        sys.error("no launch command specified")
      case launchCmd =>
        val args = javaOptions.value ++ launchCmd :+ (target in webappPrepare).value.getPath
        val process = startup(log, libs, args, containerForkOptions.value)
        instance.set(Option(process))
        process
    }
  }

  private def stopTask: Def.Initialize[Task[Unit]] = Def.task {
    shutdown(streams.value.log, containerInstance.value)
  }

  private def onLoadSetting: Def.Initialize[State => State] = Def.setting {
    (onLoad in Global).value compose { state: State =>
      state.addExitHook(shutdown(state.log, containerInstance.value))
    }
  }

  private def startup(l: Logger, libs: Seq[File], args: Seq[String], forkOptions: ForkOptions): Process = {
    l.info("starting server ...")
    val cp = libs mkString File.pathSeparator
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

  private def shutdown(l: Logger, atomicRef: AtomicReference[Option[Process]]): Unit = {
    val oldProcess = atomicRef.getAndSet(None)
    oldProcess.foreach(stopProcess(l))
  }
}
