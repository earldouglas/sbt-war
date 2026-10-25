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
import java.util.UUID
import scala.collection.concurrent.TrieMap
import scala.sys.process.{Process => ScalaProcess}

/** The top-level plugin to be used by default. From the required
  * plugins, this brings in all of the webapp components mappings, WAR
  * file packaging, and mechanisms for running both raw webapp
  * components and a packaged WAR file.
  */
object SbtWar extends AutoPlugin {

  private val containerInstances: TrieMap[UUID, ScalaProcess] =
    TrieMap.empty[UUID, ScalaProcess]

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
    lazy val warContextPath = settingKey[String]("context path")
    lazy val warStartPackage = taskKey[Unit]("start container")
    lazy val warJoin = taskKey[Unit]("join container")
    lazy val warStop = taskKey[Unit]("stop container")
    lazy val warStart = taskKey[Unit]("quickstart container")
    lazy val warProcess = settingKey[UUID]("war container instance")
    @transient
    lazy val warForkOptions =
      taskKey[ForkOptions]("container fork options")
  }

  import autoImport._
  import WebappComponentsPlugin.autoImport.servletSpec

  override val requires: Plugins =
    WarPackagePlugin

  override val projectConfigurations: Seq[Configuration] =
    Seq(War)

  private def stopContainerInstance(
      p: ScalaProcess,
      log: String => Unit
  ): Int = {
    log("[sbt-war] Stopping server")
    p.destroy()
    p.exitValue() // block until the process exits
  }

  private val runnerJars: Initialize[Task[Seq[File]]] =
    Compat.managedJars

  private val startWarFromPackage: Initialize[Task[Unit]] =
    Def.task {

      val runnerConfigFile: File = {

        val configurationFile: File =
          (Compile / target).value / "war.properties"

        Files
          .writeString(
            Paths.get(configurationFile.getPath()),
            s"""|hostname=localhost
                |port=${warPort.value}
                |contextPath=${warContextPath.value}
                |warFile=${Compat
                 .fromPackageFile(pkg)
                 .value
                 .getEscapedPath()}
                |""".stripMargin
          )
          .toFile()
      }

      val stream: TaskStreams = streams.value

      containerInstances
        .get(warProcess.value)
        .foreach { p =>
          stopContainerInstance(p, stream.log.info(_))
        }

      stream.log.info("[sbt-war] Starting server")

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

      containerInstances.put(warProcess.value, process)
    }

  private val joinWar: Initialize[Task[Option[Int]]] =
    Def.task {
      containerInstances
        .get(warProcess.value)
        .map { p =>
          p.exitValue()
        }
    }

  private val stopWar: Initialize[Task[Option[Int]]] =
    Def.task {
      val stream: TaskStreams = streams.value
      containerInstances
        .get(warProcess.value)
        .map { p =>
          stopContainerInstance(p, stream.log.info(_))
        }
    }

  private val onLoadSetting: Initialize[State => State] =
    Def.setting {
      (Global / onLoad).value
        .compose { state =>
          state.addExitHook {
            containerInstances
              .get(warProcess.value)
              .map { p =>
                stopContainerInstance(p, println(_))
              }
          }
        }
    }

  private val runnerLibrary: Initialize[ModuleID] =
    Def.setting {

      val warRunnerVersion: String =
        BuildInfo.version
          .split("-")
          .toList match {
          case v :: Nil =>
            s"""${v}_${servletSpec.value}"""
          case v :: t =>
            s"""${v}_${servletSpec.value}-${t.mkString("-")}"""
          case _ => throw new Exception("wat")
        }

      "com.earldouglas" % s"war-runner" % warRunnerVersion % War
    }

  private def startWarFromSources(
      c: Configuration
  ): Initialize[Task[Unit]] =
    Def.task {

      val runnerConfigFile: File = {

        val emptyDir: File = {
          val d = (Compile / target).value / "empty"
          if (!d.exists()) {
            d.mkdirs()
          }
          d
        }

        val resourceMapString =
          WebappComponentsPlugin
            .warContents(c)
            .value
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
                |contextPath=${warContextPath.value}
                |emptyWebappDir=${emptyDir.getEscapedPath()}
                |emptyClassesDir=${emptyDir.getEscapedPath()}
                |resourceMap=${resourceMapString}
                |""".stripMargin
          )
          .toFile()
      }

      val stream: TaskStreams = streams.value

      containerInstances
        .get(warProcess.value)
        .foreach { p =>
          stopContainerInstance(p, stream.log.info(_))
        }

      stream.log.info("[sbt-war] Quickstarting server")

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

      containerInstances.put(warProcess.value, process)
    }

  def settingsFor(c: Configuration): Seq[Setting[?]] =
    Seq(
      Seq(
        c / warStart := startWarFromSources(c).value
      ),
      WebappComponentsPlugin.settingsFor(c)
    ).flatten

  override val projectSettings: Seq[Setting[?]] =
    Seq(
      Seq(
        Global / onLoad := onLoadSetting.value,
        libraryDependencies += runnerLibrary.value,
        warForkOptions := forkOptions.value,
        warJoin := joinWar.value.getOrElse(()),
        warPort := 8080,
        warContextPath := "",
        warStartPackage := startWarFromPackage.value,
        warStop := stopWar.value.getOrElse(()),
        warProcess := UUID.randomUUID()
      ),
      settingsFor(Runtime),
      settingsFor(Test)
    ).flatten

}
