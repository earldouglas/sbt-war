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

  import WebappPlugin.autoImport.webappPrepare
  import autoImport._

  override def requires = WebappPlugin

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
        start            <<= startTask(atomicRef) dependsOn webappPrepare
      , stop             <<= stopTask(atomicRef)
      , onLoad in Global <<= onLoadSetting(atomicRef)
      , javaOptions      <<= javaOptions in Compile
      ))
  }

  lazy val baseContainerSettings = Seq(
    containerPort        := -1
  , containerConfigFile  := None
  , containerArgs        := Nil
  , containerForkOptions := new ForkOptions
  )


  private def shutdown(l: Logger, atomicRef: AtomicReference[Option[Process]]): Unit = {
    val oldProcess = atomicRef.getAndSet(None)
    oldProcess.foreach(stopProcess(l))
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

  private def startup(l: Logger, libs: Seq[File], args: Seq[String], forkOptions: ForkOptions): Process = {
    l.info("starting server ...")
    val cp = libs mkString File.pathSeparator
    new Fork("java", None).fork(forkOptions, Seq("-cp", cp) ++ args)
  }

  private def startTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Process]] =
      ( containerLaunchCmd
      , target in webappPrepare
      , javaOptions
      , classpathTypes
      , update
      , containerForkOptions
      , streams
      , configuration
      ) map {
        ( launchCmd
        , webappTarget
        , javaOptions
        , classpathTypes
        , updateReport
        , forkOptions
        , streams
        , conf
        ) =>
          val log = streams.log

          shutdown(log, atomicRef)
          log.info(s"conf: $conf")

          val libs: Seq[File] =
            Classpaths.managedJars(conf, classpathTypes, updateReport).map(_.data)

          log.info(s"libs: $libs")

          launchCmd match {
            case Nil =>
              sys.error("no launch command specified")
            case args =>
              val p = startup(log, libs, javaOptions ++ args :+ webappTarget.getPath, forkOptions)
              atomicRef.set(Option(p))
              p
          }
      }

  private def stopTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Unit]] = Def.task {
    shutdown(streams.value.log, atomicRef)
  }

  private def onLoadSetting(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[State => State] = Def.setting {
    (onLoad in Global).value compose { state: State =>
      state.addExitHook(shutdown(state.log, atomicRef))
    }
  }
}
