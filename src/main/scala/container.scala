package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File

trait ContainerPlugin { self: WebappPlugin =>

  lazy val container  = config("container").hide
  lazy val start      = TaskKey[Process]("start")
  lazy val stop       = TaskKey[Unit]("stop")
  lazy val launcher   = TaskKey[Seq[String]]("launcher")

  private var process: Option[Process] = None

  private def shutdown(l: Logger)(p: Process): Unit = {
    l.info("waiting for server to shut down...")
    p.destroy
    p.exitValue
  }

  private def startup(l: Logger, libs: Seq[File], args: Seq[String]): Process = {
    l.info("starting server...")
    val cp = libs mkString File.pathSeparator
    Fork.java.fork(new ForkOptions, Seq("-cp", cp) ++ args)
 }

  lazy val startTask: Def.Initialize[Task[Process]] =
    (   webappSrc in webapp
      , launcher in container
      , classpathTypes in container
      , update in container
      , streams
    ) map { (webappSrc, launcher, classpathTypes, updateReport, streams) =>
      process synchronized {
        process foreach { shutdown(streams.log) }
        val libs: Seq[File] =
          Classpaths.managedJars(container, classpathTypes, updateReport).map(_.data)
        launcher match {
          case Nil =>
            sys.error("no launcher specified")
          case args =>
            val p = startup(streams.log, libs, args)
            process = Some(p)
            p
        }
      }
    }

  lazy val stopTask: Def.Initialize[Task[Unit]] =
    (streams) map { (streams) =>
      process synchronized {
        process foreach { shutdown(streams.log) }
        process = None
      }
    }

  def containerSettings(
      launcherTask: Def.Initialize[Task[Seq[String]]]
  ): Seq[Setting[_]] =
    inConfig(container) {
      Seq(start    <<= startTask dependsOn (prepareWebapp in webapp)
        , stop     <<= stopTask
        , launcher <<= launcherTask
      )
    } ++ Seq(ivyConfigurations += container)

  def runnerContainer(
    port: Int, runner: Option[ModuleID], main: String
  ): Seq[Setting[_]] =
    (runner map { x => libraryDependencies += x }).toSeq ++
    containerSettings(
      (webappDest in webapp) map { d =>
        Seq(main, "--port", port.toString , d.getPath)
      }
    )

}
