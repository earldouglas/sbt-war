package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.settingKey
import sbt.Keys._
import sbt._

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import scala.sys.process.{Process => ScalaProcess}

/** Launches the webapp file managed by WebappComponentsPlugin. Uses a
  * forked JVM to run Tomcat via
  * com.earldouglas:webapp-components-runner.
  */
object WebappComponentsRunnerPlugin extends AutoPlugin {

  object autoImport {
    lazy val Webapp = config("webapp").hide
    lazy val webappPort = settingKey[Int]("webapp container port")
    lazy val webappStart = taskKey[Unit]("start webapp container")
    lazy val webappJoin = taskKey[Unit]("join webapp container")
    lazy val webappStop = taskKey[Unit]("stop webapp container")
    lazy val webappForkOptions =
      settingKey[ForkOptions]("webapp container fork options")
    lazy val webappComponentsRunnerVersion =
      settingKey[String]("webapp-components-runner version")
  }

  import autoImport._
  import WebappRunnerPlugin.autoImport._

  override val requires: Plugins =
    WebappComponentsPlugin && WebappRunnerPlugin

  override val projectConfigurations: Seq[Configuration] =
    Seq(Webapp)

  private lazy val containerInstance =
    new AtomicReference[Option[ScalaProcess]](None)

  override val projectSettings: Seq[Setting[_]] = {

    def stopContainerInstance(): Unit = {
      val oldProcess = containerInstance.getAndSet(None)
      oldProcess.foreach(_.destroy())
    }

    val runnerConfigFile: Initialize[Task[File]] =
      Def.task {

        val emptyDir: File = (Compile / target).value / "empty"

        val resourceMapString =
          WebappComponentsPlugin.webappContents.value
            .map { case (k, v) =>
              s"${k}->${v}"
            }
            .mkString(",")

        val configurationFile: File =
          (Compile / target).value / "webapp-components.properties"

        Files
          .writeString(
            Paths.get(configurationFile.getPath()),
            s"""|hostname=localhost
              |port=${webappPort.value}
              |contextPath=
              |emptyWebappDir=${emptyDir}
              |emptyClassesDir=${emptyDir}
              |resourceMap=${resourceMapString}
              |""".stripMargin
          )
          .toFile()
      }

    val startWebapp: Initialize[Task[Unit]] =
      Def.task {
        stopContainerInstance()

        val runnerJars: Seq[File] =
          Classpaths
            .managedJars(Webapp, classpathTypes.value, update.value)
            .map(_.data)
            .toList

        streams.value.log.info("[sbt-war] Starting server")
        val process: ScalaProcess =
          Fork.java.fork(
            webappForkOptions.value,
            Seq(
              "-cp",
              Path.makeString(runnerJars),
              "com.earldouglas.WebappComponentsRunner",
              runnerConfigFile.value.getPath()
            )
          )
        containerInstance.set(Some(process))
      }

    val joinWebapp: Initialize[Task[Unit]] =
      Def.task(containerInstance.get.map(_.exitValue))

    val stopWebapp: Initialize[Task[Unit]] =
      Def.task(stopContainerInstance())

    val onLoadSetting: Initialize[State => State] =
      Def.setting {
        (Global / onLoad).value
          .compose { state: State =>
            state.addExitHook(stopContainerInstance())
          }
      }

    Seq(
      webappPort := 8080,
      webappStart := startWebapp.value,
      webappJoin := joinWebapp.value,
      webappStop := stopWebapp.value,
      webappForkOptions := ForkOptions(),
      Global / onLoad := onLoadSetting.value,
      webappComponentsRunnerVersion := BuildInfo.webappComponentsRunnerVersion,
      libraryDependencies ++=
        Seq(
          "com.earldouglas" % "webapp-components-runner" % webappComponentsRunnerVersion.value % Webapp,
          "com.heroku" % "webapp-runner" % webappRunnerVersion.value % Webapp
        )
    )
  }
}
