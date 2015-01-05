package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File
import java.util.concurrent.atomic.AtomicReference

trait ContainerPlugin { self: WebappPlugin =>

  lazy val container  = config("container").hide
  lazy val start      = TaskKey[Process]("start")
  lazy val develop      = TaskKey[Process]("develop")
  lazy val stop       = TaskKey[Unit]("stop")
  lazy val launchCmd  = TaskKey[Seq[String]]("launch-cmd")
  lazy val options    = TaskKey[ForkOptions]("options")
  lazy val dependencyCmd  = TaskKey[Seq[String]]("dependency-cmd")

  private def shutdown(l: Logger, atomicRef: AtomicReference[Option[Process]]): Unit = {
    val oldProcess = atomicRef.getAndSet(None)
    oldProcess.foreach(stopProcess(l))
  }

  private def stopProcess(l: Logger)(p: Process): Unit = {
    l.info("waiting for server to shut down...")
    p.destroy
    p.exitValue
  }

  private def startup(
                       l: Logger, cp: String, args: Seq[String], forkOptions: ForkOptions
                       ): Process = {
    l.info("starting server ...")
    Fork.java.fork(forkOptions, Seq("-cp", cp) ++ args)
  }

  def developTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Process]] =
    (  launchCmd in container
      , dependencyCmd in container
      , javaOptions in container
      , classpathTypes in container
      , update in container
      , options in container
      , streams
      ) map {
      (  launchCmd
         , dependencyCmd
         , javaOptions
         , classpathTypes
         , updateReport
         , forkOptions
         , streams
        ) =>
        shutdown(streams.log, atomicRef)

        val libs: Seq[File] =
          Classpaths.managedJars(container, classpathTypes, updateReport).map(_.data)


        launchCmd match {
          case Nil =>
            sys.error("no launch command specified")
          case args =>
            dependencyCmd match {
              case Nil =>
                sys.error("no launch command specified")
              case dependencies =>
                val cp = (dependencies mkString File.pathSeparator) + File.pathSeparator + libs.mkString(File.pathSeparator)
                val p = startup(streams.log, cp, javaOptions  ++ args , forkOptions)
                atomicRef.set(Option(p))
                p
            }
        }
    }


  def startTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Process]] =
    (  launchCmd in container
      , javaOptions in container
      , classpathTypes in container
      , update in container
      , options in container
      , streams
      ) map {
      (  launchCmd
         , javaOptions
         , classpathTypes
         , updateReport
         , forkOptions
         , streams
        ) =>
        shutdown(streams.log, atomicRef)

        val libs: Seq[File] =
          Classpaths.managedJars(container, classpathTypes, updateReport).map(_.data)

        launchCmd match {
          case Nil =>
            sys.error("no launch command specified")
          case args =>
            val cp =  libs.mkString(File.pathSeparator)
            val p = startup(streams.log, cp, javaOptions ++ args, forkOptions)
            atomicRef.set(Option(p))
            p
        }
    }

  def stopTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Unit]] = Def.task {
    shutdown(streams.value.log, atomicRef)
  }

  def onLoadSetting(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[State => State] = Def.setting {
    (onLoad in Global).value compose { state: State =>
      state.addExitHook(shutdown(state.log, atomicRef))
    }
  }

  def containerSettings(
                         launchCmdTask: Def.Initialize[Task[Seq[String]]],
                         forkOptions: ForkOptions
                         ): Seq[Setting[_]] = {
    val atomicRef: AtomicReference[Option[Process]] = new AtomicReference(None)

    inConfig(container) {
      Seq(start            <<= startTask(atomicRef) dependsOn (prepareWebapp in webapp)
        , stop             <<= stopTask(atomicRef)
        , launchCmd        <<= launchCmdTask
        , options           := forkOptions
        , onLoad in Global <<= onLoadSetting(atomicRef)
        , javaOptions      <<= javaOptions in Compile
      )
    } ++ Seq(ivyConfigurations += container)
  }

  def developContainerSettings(
                                launchCmdTask: Def.Initialize[Task[Seq[String]]],
                                dependencyCmdTask: Def.Initialize[Task[Seq[String]]],
                                forkOptions: ForkOptions
                                ): Seq[Setting[_]] = {
    val atomicRef: AtomicReference[Option[Process]] = new AtomicReference(None)

    inConfig(container) {
      Seq(start            <<= startTask(atomicRef) dependsOn (prepareWebapp in webapp)
        , develop            <<= developTask(atomicRef)
        , stop             <<= stopTask(atomicRef)
        , launchCmd        <<= launchCmdTask
        , dependencyCmd    <<= dependencyCmdTask
        , options           := forkOptions
        , onLoad in Global <<= onLoadSetting(atomicRef)
        , javaOptions      <<= javaOptions in Compile
      )
    } ++ Seq(ivyConfigurations += container)
  }


  def runnerContainer(
                       libs: Seq[ModuleID], args: Seq[String], forkOptions: ForkOptions = new ForkOptions
                       ): Seq[Setting[_]] =
    Seq(libraryDependencies ++= libs) ++
      containerSettings((webappDest in webapp) map { d => args :+ d.getPath }, forkOptions)

  def developContainer(
                        libs: Seq[ModuleID], args: Seq[String], forkOptions: ForkOptions = new ForkOptions
                        ): Seq[Setting[_]] = {

    Seq(libraryDependencies ++= libs) ++
      developContainerSettings((webappSrc in webapp) map { d => args :+ d.getPath },
        (fullClasspath in Runtime) map { cp =>
          cp.map(_.data.getPath)
        },
        forkOptions)
  }

}
