package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.settingKey
import sbt.Def.taskKey
import sbt.Keys._
import sbt._

import java.util.concurrent.atomic.AtomicReference

object WebappComponentsRunnerPlugin extends AutoPlugin {

  object autoImport {
    lazy val webappPort = settingKey[Int]("container port")
    lazy val webappStart = taskKey[Unit]("start container")
    lazy val webappJoin = taskKey[Unit]("join container")
    lazy val webappStop = taskKey[Unit]("stop container")
  }

  import autoImport._

  override val requires: Plugins = WebappComponentsPlugin

  private lazy val containerInstance =
    new AtomicReference[Option[WebappComponentsRunner]](None)

  private val startTask: Initialize[Task[Unit]] =
    Def.task {
      stopContainerInstance()

      val runner: WebappComponentsRunner =
        WebappComponentsRunner(
          webappPort.value,
          java.nio.file.Files.createTempDirectory(null).toFile(),
          java.nio.file.Files.createTempDirectory(null).toFile(),
          WebappComponentsPlugin.webappContents.value
        )
      runner.start()

      containerInstance.set(Some(runner))
    }

  private val joinTask: Initialize[Task[Unit]] =
    Def.task {
      containerInstance.get.foreach(_.join())
    }

  private def stopContainerInstance(): Unit = {
    val oldProcess = containerInstance.getAndSet(None)
    oldProcess.foreach(_.stop())
  }

  private val stopTask: Initialize[Task[Unit]] =
    Def.task {
      stopContainerInstance()
    }

  private val onLoadSetting: Initialize[State => State] =
    Def.setting {
      (Global / onLoad).value compose { state: State =>
        state.addExitHook(stopContainerInstance())
      }
    }

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      webappPort := 8080,
      webappStart := startTask.value,
      webappJoin := joinTask.value,
      webappStop := stopTask.value,
      Global / onLoad := onLoadSetting.value
    )
}
