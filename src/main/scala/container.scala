package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File
import java.util.concurrent.atomic.AtomicReference

trait ContainerPlugin { self: WebappPlugin =>

  lazy val container  = config("container").hide
  lazy val start      = TaskKey[Process]("start")
  lazy val stop       = TaskKey[Unit]("stop")
  lazy val launcher   = TaskKey[Seq[String]]("launcher")

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
    l: Logger, libs: Seq[File], args: Seq[String]
  ): Process = {
    l.info("starting server ...")
    val cp = libs mkString File.pathSeparator
    Fork.java.fork(new ForkOptions, Seq("-cp", cp) ++ args)
  }

  def startTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Process]] =
    (  launcher in container
     , javaOptions in container
     , classpathTypes in container
     , update in container
     , streams
    ) map {
      (  launcher
       , javaOptions
       , classpathTypes
       , updateReport
       , streams
      ) =>
        shutdown(streams.log, atomicRef)

        val libs: Seq[File] =
          Classpaths.managedJars(container, classpathTypes, updateReport).map(_.data)

        launcher match {
          case Nil =>
            sys.error("no launcher specified")
          case args =>
            val p = startup(streams.log, libs, javaOptions ++ args)
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
      launcherTask: Def.Initialize[Task[Seq[String]]]
  ): Seq[Setting[_]] = {
    val atomicRef: AtomicReference[Option[Process]] = new AtomicReference(None)

    inConfig(container) {
      Seq(start    <<= startTask(atomicRef) dependsOn (prepareWebapp in webapp)
        , stop     <<= stopTask(atomicRef)
        , launcher <<= launcherTask
        , onLoad in Global <<= onLoadSetting(atomicRef)
        , javaOptions <<= javaOptions in Compile
      )
    } ++ Seq(ivyConfigurations += container)
  }

  def runnerContainer(
    libs: Seq[ModuleID], args: Seq[String]
  ): Seq[Setting[_]] =
    Seq(libraryDependencies ++= libs) ++
    containerSettings((webappDest in webapp) map { d => args :+ d.getPath })

}
