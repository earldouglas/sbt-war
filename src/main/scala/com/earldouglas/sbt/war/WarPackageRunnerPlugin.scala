package com.earldouglas.sbt.war

import sbt.Def.settingKey
import sbt.Def.taskKey
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt._

import java.util.concurrent.atomic.AtomicReference
import scala.sys.process.{Process => ScalaProcess}

/** Launches the .war file managed by WarPackagePlugin. Uses a forked
  * JVM to run Tomcat via webapp-runner.
  */
object WarPackageRunnerPlugin extends AutoPlugin {

  object autoImport {
    lazy val War = config("war").hide
    lazy val port = settingKey[Int]("container port")
    lazy val start = taskKey[Unit]("start container")
    lazy val join = taskKey[Option[Int]]("join container")
    lazy val stop = taskKey[Unit]("stop container")
    lazy val webappRunnerVersion =
      settingKey[String]("webapp-runner version")
  }

  import autoImport._

  override val requires: Plugins = WarPackagePlugin
  override val projectConfigurations: Seq[Configuration] = Seq(War)

  private lazy val containerInstance =
    new AtomicReference[Option[ScalaProcess]](
      Option.empty[ScalaProcess]
    )

  private val startTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val runners: Seq[File] =
        Classpaths
          .managedJars(
            configuration.value,
            classpathTypes.value,
            update.value
          )
          .map(_.data)
          .toList

      runners match {
        case r :: Nil =>
          streams.value.log.info("[sbt-war] Starting server")
          val process: ScalaProcess =
            Fork.java.fork(
              (War / forkOptions).value,
              Seq("-jar", r.file.getPath(), (War / pkg).value.getPath())
            )
          containerInstance.set(Some(process))
        case _ :: _ =>
          streams.value.log.error(
            s"""[sbt-war] Expected one runner, but found ${runners.length}: ${runners
                .mkString("\n  * ", "  * ", "")}"""
          )
        case _ =>
          streams.value.log.error(
            """[sbt-war] Expected one runner, but found none"""
          )
      }
    }

  private val joinTask: Def.Initialize[Task[Option[Int]]] =
    Def.task {
      containerInstance.get.map { _.exitValue }
    }

  private def stopContainerInstance(): Unit = {
    val oldProcess =
      containerInstance
        .getAndSet(Option.empty[ScalaProcess])
    oldProcess.foreach(_.destroy())
  }

  private val stopTask: Def.Initialize[Task[Unit]] =
    Def.task {
      stopContainerInstance()
    }

  private val onLoadSetting: Def.Initialize[State => State] =
    Def.setting {
      (Global / onLoad).value compose { state: State =>
        state.addExitHook(stopContainerInstance())
      }
    }

  override lazy val projectSettings =
    Seq(
      inConfig(War)(
        Seq(
          port := 8080,
          start := startTask.value,
          join := joinTask.value,
          stop := stopTask.value,
          forkOptions := ForkOptions(),
          webappRunnerVersion := "9.0.68.1"
        )
      ),
      Seq(
        Global / onLoad := onLoadSetting.value,
        libraryDependencies +=
          ("com.heroku" % "webapp-runner" % (War / webappRunnerVersion).value intransitive ()) % War
      )
    ).flatten
}
