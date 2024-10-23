package com.earldouglas.sbt.war

import sbt.AutoPlugin
import sbt.Def.Initialize
import sbt.Def.settingKey
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt.Plugins
import sbt._

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

  object autoImport {
    lazy val War = config("war").hide
    lazy val servletSpec = settingKey[String]("servlet spec version")
    lazy val warPort = settingKey[Int]("container port")
    lazy val warStart = taskKey[Unit]("start container")
    lazy val warJoin = taskKey[Unit]("join container")
    lazy val warStop = taskKey[Unit]("stop container")
    lazy val warQuickstart = taskKey[Unit]("quickstart container")
    lazy val warForkOptions =
      taskKey[ForkOptions]("container fork options")
  }

  import autoImport._

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
                  |warFile=${pkg.value.getPath()}
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

    val runnerLibraries: Initialize[Seq[ModuleID]] =
      Def.setting {

        val servletApi: ModuleID =
          servletSpec.value match {
            case "3.0" =>
              "javax.servlet" % "javax.servlet-api" % "3.0.1"
            case "3.1" =>
              "javax.servlet" % "javax.servlet-api" % "3.1.0"
            case "4.0" =>
              "jakarta.servlet" % "jakarta.servlet-api" % "4.0.4"
            case "6.0" =>
              "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0"
          }

        val warRunnerVersion: String =
          s"${servletSpec.value}_${BuildInfo.version}"

        Seq(
          "com.earldouglas" % s"war-runner" % warRunnerVersion % War,
          servletApi % Provided
        )
      }

    val quickstartWar: Initialize[Task[Unit]] =
      Def.task {

        val runnerConfigFile: File = {

          val emptyDir: File = (Compile / target).value / "empty"

          val resourceMapString =
            WebappComponentsPlugin.warContents.value
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
                  |port=${warPort.value}
                  |contextPath=
                  |emptyWebappDir=${emptyDir}
                  |emptyClassesDir=${emptyDir}
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
      libraryDependencies ++= runnerLibraries.value,
      servletSpec := "6.0",
      warForkOptions := forkOptions.value,
      warJoin := joinWar.value,
      warPort := 8080,
      warQuickstart := quickstartWar.value,
      warStart := startWar.value,
      warStop := stopWar.value
    )
  }
}
