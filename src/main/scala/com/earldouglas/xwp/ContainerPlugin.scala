package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File
import java.util.concurrent.atomic.AtomicReference

trait ContainerPlugin { self: WebappPlugin =>

  lazy val Container  = config("container").hide
  lazy val start      = taskKey[Process]("start container")
  lazy val stop       = taskKey[Unit]("stop container")
  lazy val launchCmd  = taskKey[Seq[String]]("xwp-launcher")
  lazy val options    = taskKey[ForkOptions]("xwp-options")

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

  private def startup(
    l: Logger, libs: Seq[File], args: Seq[String], forkOptions: ForkOptions
  ): Process = {
    l.info("starting server ...")
    val cp = libs mkString File.pathSeparator
    new Fork("java", None).fork(forkOptions, Seq("-cp", cp) ++ args)
  }

  def startTask(atomicRef: AtomicReference[Option[Process]]): Def.Initialize[Task[Process]] =
    (  launchCmd in Container
     , javaOptions in Container
     , classpathTypes in Container
     , update in Container
     , options in Container
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
          Classpaths.managedJars(Container, classpathTypes, updateReport).map(_.data)

        launchCmd match {
          case Nil =>
            sys.error("no launch command specified")
          case args =>
            val p = startup(streams.log, libs, javaOptions ++ args, forkOptions)
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

    inConfig(Container) {
      Seq(start            <<= startTask(atomicRef) dependsOn webappPrepare
        , stop             <<= stopTask(atomicRef)
        , launchCmd        <<= launchCmdTask
        , options           := forkOptions
        , onLoad in Global <<= onLoadSetting(atomicRef)
        , javaOptions      <<= javaOptions in Compile
      )
    } ++ Seq(ivyConfigurations += Container)
  }

  def runnerContainer(
    libs: Seq[ModuleID], args: Seq[String], forkOptions: ForkOptions = new ForkOptions
  ): Seq[Setting[_]] =
    Seq(libraryDependencies ++= libs) ++
    containerSettings(target in webappPrepare map { d => args :+ d.getPath }, forkOptions)

}
