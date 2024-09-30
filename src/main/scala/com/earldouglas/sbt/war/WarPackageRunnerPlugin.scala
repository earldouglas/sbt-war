package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.settingKey
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
    lazy val warPort = settingKey[Int]("war container port")
    lazy val warStart = taskKey[Unit]("start war container")
    lazy val warJoin = taskKey[Unit]("join war container")
    lazy val warStop = taskKey[Unit]("stop war container")
  }

  import autoImport._
  import WebappRunnerPlugin.autoImport._

  override val requires: Plugins =
    WarPackagePlugin && WebappRunnerPlugin

  override val projectConfigurations: Seq[Configuration] = Seq(War)

  private lazy val containerInstance =
    new AtomicReference[Option[ScalaProcess]](None)

  private val startWar: Initialize[Task[Unit]] =
    Def.task {
      stopContainerInstance()

      val runners: Seq[File] =
        Classpaths
          .managedJars(War, classpathTypes.value, update.value)
          .map(_.data)
          .toList

      runners match {
        case runner :: Nil =>
          streams.value.log.info("[sbt-war] Starting server")
          val process: ScalaProcess =
            Fork.java.fork(
              (War / forkOptions).value,
              Seq(
                "-jar",
                runner.file.getPath(),
                "--port",
                warPort.value.toString(),
                pkg.value.getPath()
              )
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

  private val joinWar: Initialize[Task[Unit]] =
    Def.task(containerInstance.get.map(_.exitValue))

  private def stopContainerInstance(): Unit = {
    val oldProcess = containerInstance.getAndSet(None)
    oldProcess.foreach(_.destroy())
  }

  private val stopWar: Initialize[Task[Unit]] =
    Def.task(stopContainerInstance())

  private val onLoadSetting: Initialize[State => State] =
    Def.setting {
      (Global / onLoad).value
        .compose { state: State =>
          state.addExitHook(stopContainerInstance())
        }
    }

  override lazy val projectSettings =
    Seq(
      warPort := 8080,
      warStart := startWar.value,
      warJoin := joinWar.value,
      warStop := stopWar.value,
      War / forkOptions := ForkOptions(),
      Global / onLoad := onLoadSetting.value,
      libraryDependencies +=
        ("com.heroku" % "webapp-runner" % (War / webappRunnerVersion).value intransitive ()) % War
    )
}
