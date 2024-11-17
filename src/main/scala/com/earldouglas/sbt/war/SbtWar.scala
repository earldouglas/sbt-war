package com.earldouglas.sbt.war

import sbt.AutoPlugin
import sbt.Def.Initialize
import sbt.Def.settingKey
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt.Plugins
import sbt._

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import scala.sys.process.{Process => ScalaProcess}

/** The top-level plugin to be used by default. From the required
  * plugins, this brings in all of the webapp components mappings, .war
  * file packaging, and mechanisms for running both raw webapp
  * components and a packaged .war file.
  */
object SbtWar extends AutoPlugin {

  implicit class Exscape(val x: File) {
    def getEscapedPath(): String =
      if (File.separatorChar == '\\') {
        x.getPath().replace("\\", "\\\\")
      } else {
        x.getPath()
      }
  }

  object autoImport {
    lazy val War = config("war").hide
    lazy val warPort = settingKey[Int]("container port")
    lazy val warStart = taskKey[Unit]("start container")
    lazy val warJoin = taskKey[Unit]("join container")
    lazy val warStop = taskKey[Unit]("stop container")
    lazy val warQuickstart = taskKey[Unit]("quickstart container")
    lazy val warForkOptions =
      taskKey[ForkOptions]("container fork options")
  }

  import autoImport._
  import WebappComponentsPlugin.autoImport.servletSpec

  override val requires: Plugins =
    WarPackagePlugin

  override val projectConfigurations: Seq[Configuration] =
    Seq(War)

  private lazy val containerInstance =
    new AtomicReference[Option[ScalaProcess]](None)

  override val projectSettings: Seq[Setting[_]] = {

    def stopContainerInstance(log: String => Unit): Unit =
      containerInstance
        .getAndSet(None)
        .foreach { p =>
          log("[sbt-war] Starting server")
          p.destroy()
        }

    val runnerJars: Initialize[Task[Seq[File]]] =
      Def.task {
        Classpaths
          .managedJars(War, classpathTypes.value, update.value)
          .map(_.data)
          .toList
      }

    val startWar: Initialize[Task[Unit]] =
      Def.task {

        val runnerConfigFile: File = {

          val configurationFile: File =
            (Compile / target).value / "war.properties"

          Files
            .writeString(
              Paths.get(configurationFile.getPath()),
              s"""|port=${warPort.value}
                  |warFile=${pkg.value.getEscapedPath()}
                  |""".stripMargin
            )
            .toFile()
        }

        stopContainerInstance(streams.value.log.info(_))

        streams.value.log.info("[sbt-war] Starting server")
        val process: ScalaProcess =
          Fork.java.fork(
            warForkOptions.value,
            Seq(
              "-cp",
              Path.makeString(runnerJars.value),
              "com.earldouglas.WarRunner",
              runnerConfigFile.getPath()
            )
          )
        containerInstance.set(Some(process))
      }

    val joinWar: Initialize[Task[Unit]] =
      Def.task(containerInstance.get.map(_.exitValue()))

    val stopWar: Initialize[Task[Unit]] =
      Def.task(stopContainerInstance(streams.value.log.info(_)))

    val onLoadSetting: Initialize[State => State] =
      Def.setting {
        (Global / onLoad).value
          .compose { state: State =>
            state.addExitHook(stopContainerInstance(println(_)))
          }
      }

    val forkOptions: Initialize[Task[ForkOptions]] =
      Def.task {
        ForkOptions()
          .withOutputStrategy(Some(BufferedOutput(streams.value.log)))
      }

    val runnerLibrary: Initialize[ModuleID] =
      Def.setting {
        val warRunnerVersion: String =
          s"${BuildInfo.version}_${servletSpec.value}"
        "com.earldouglas" % s"war-runner" % warRunnerVersion % War
      }

    val quickstartWar: Initialize[Task[Unit]] =
      Def.task {

        val runnerConfigFile: File = {

          val emptyDir: File = (Compile / target).value / "empty"

          val resourceMapString =
            WebappComponentsPlugin.warContents.value
              .map { case (k, v) =>
                s"${k}->${v.getEscapedPath()}"
              }
              .mkString(",")

          val configurationFile: File =
            (Compile / target).value / "webapp-components.properties"

          Files
            .writeString(
              Paths.get(configurationFile.getPath()),
              s"""|hostname=localhost
                  |port=${warPort.value}
                  |contextPath=
                  |emptyWebappDir=${emptyDir.getEscapedPath()}
                  |emptyClassesDir=${emptyDir.getEscapedPath()}
                  |resourceMap=${resourceMapString}
                  |""".stripMargin
            )
            .toFile()
        }

        stopContainerInstance(streams.value.log.info(_))

        streams.value.log.info("[sbt-war] Quickstarting server")
        val process: ScalaProcess =
          Fork.java.fork(
            warForkOptions.value,
            Seq(
              "-cp",
              Path.makeString(runnerJars.value),
              "com.earldouglas.WebappComponentsRunner",
              runnerConfigFile.getPath()
            )
          )
        containerInstance.set(Some(process))
      }

    Seq(
      Global / onLoad := onLoadSetting.value,
      libraryDependencies += runnerLibrary.value,
      warForkOptions := forkOptions.value,
      warJoin := joinWar.value,
      warPort := 8080,
      warQuickstart := quickstartWar.value,
      warStart := startWar.value,
      warStop := stopWar.value
    )
  }
}
