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
      Compat.managedJars(War)

    val startWar: Initialize[Task[Unit]] =
      Def.task {
        stopContainerInstance(streams.value.log.info(_))

        streams.value.log.info("[sbt-war] Starting server")
        val process: ScalaProcess =
          Fork.java.fork(
            warForkOptions.value,
            Seq(
              "-cp",
              Path.makeString(runnerJars.value),
              "webapp.runner.launch.Main",
              "--port",
              warPort.value.toString(),
              Compat
                .toFile(pkg)
                .value
                .getPath()
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
        Compat.Global_onLoad.value
          .compose(_.addExitHook(stopContainerInstance(println(_))))
      }

    val forkOptions: Initialize[Task[ForkOptions]] =
      Def.task {
        ForkOptions()
          .withOutputStrategy(Some(BufferedOutput(streams.value.log)))
      }

    val runnerLibraries: Initialize[Seq[ModuleID]] =
      Def.setting {
        Seq(
          "com.earldouglas" % "webapp-components-runner" % BuildInfo.warRunnerVersion % War
        )
      }

    val quickstartWar: Initialize[Task[Unit]] =
      Def.task {

        val runnerConfigFile: File = {

          val emptyDir: File = (Compat.Compile_target).value / "empty"

          val resourceMapString =
            Compat.warContents.value
              .map { case (k, v) =>
                s"${k}->${v}"
              }
              .mkString(",")

          val configurationFile: File =
            (Compat.Compile_target).value / "webapp-components.properties"

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
      Compat.Global_onLoad := onLoadSetting.value,
      libraryDependencies ++= runnerLibraries.value,
      warForkOptions := forkOptions.value,
      warJoin := joinWar.value,
      warPort := 8080,
      warQuickstart := quickstartWar.value,
      warStart := startWar.value,
      warStop := stopWar.value
    )
  }
}
