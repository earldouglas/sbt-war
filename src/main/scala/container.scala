package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File

import scala.collection.mutable.{ Map => MMap}
import scala.collection.mutable.{ HashMap => MHMap}

trait ContainerPlugin { self: WebappPlugin =>

  lazy val container  = config("container").hide
  lazy val start      = TaskKey[Process]("start")
  lazy val stop       = TaskKey[Unit]("stop")
  lazy val launcher   = TaskKey[Seq[String]]("launcher")

  private val processes: MMap[String,Process] = MHMap.empty

  private def shutdown(id: String, l: Logger)(p: Process): Unit = {
    l.info("waiting for server " + id + " to shut down...")
    p.destroy
    p.exitValue
  }

  private def startup(
    id: String, l: Logger, libs: Seq[File], args: Seq[String]
  ): Process = {
    l.info("starting server " + id + "...")
    val cp = libs mkString File.pathSeparator
    Fork.java.fork(new ForkOptions, Seq("-cp", cp) ++ args)
 }

  lazy val startTask: Def.Initialize[Task[Process]] =
    (  thisProject
     , launcher in container
     , classpathTypes in container
     , update in container
     , streams
    ) map {
      (  thisProject
       , launcher
       , classpathTypes
       , updateReport
       , streams
      ) =>
        val id = thisProject.id
        synchronized {
          processes.get(id) foreach { shutdown(id, streams.log) }
          val libs: Seq[File] =
            Classpaths.managedJars(container, classpathTypes, updateReport).map(_.data)
          launcher match {
            case Nil =>
              sys.error("no launcher specified")
            case args =>
              val p = startup(id, streams.log, libs, args)
              processes(id) = p
              p
          }
        }
      }

  lazy val stopTask: Def.Initialize[Task[Unit]] =
    (thisProject, streams) map {
      (thisProject, streams) =>
        val id = thisProject.id
        synchronized {
          processes.get(id) foreach { shutdown(id, streams.log) }
          processes.remove(id)
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
    libs: Seq[ModuleID], args: Seq[String]
  ): Seq[Setting[_]] =
    Seq(libraryDependencies ++= libs) ++
    containerSettings((webappDest in webapp) map { d => args :+ d.getPath })

}
