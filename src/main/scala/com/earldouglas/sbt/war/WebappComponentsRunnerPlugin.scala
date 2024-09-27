package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.settingKey
import sbt.Def.taskKey
import sbt.Keys._
import sbt._

import java.util.concurrent.atomic.AtomicReference

/** Launches the webapp composed of the resources, classes, and
  * libraries specified by WebappComponentsPlugin.
  */
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

  private val startWebapp: Initialize[Task[Unit]] =
    Def.task {
      stopContainerInstance()

      val emptyDir: File =
        WebappComponentsRunner.mkdir(
          (Compile / target).value / "empty"
        )

      val runner: WebappComponentsRunner =
        WebappComponentsRunner(
          hostname = "localhost", // TODO this could be a settingKey
          port = webappPort.value,
          contextPath = "", // TODO this could be a settingKey
          emptyWebappDir = emptyDir,
          emptyClassesDir = emptyDir,
          resourceMap = WebappComponentsPlugin.webappContents.value
        )
      runner.start()

      containerInstance.set(Some(runner))
    }

  private val joinWebapp: Initialize[Task[Unit]] =
    Def.task(containerInstance.get.foreach(_.join()))

  private def stopContainerInstance(): Unit =
    containerInstance.getAndSet(None).foreach(_.stop())

  private val stopWebapp: Initialize[Task[Unit]] =
    Def.task(stopContainerInstance())

  private val onLoadSetting: Initialize[State => State] =
    Def.setting {
      (Global / onLoad).value compose { state: State =>
        state.addExitHook(stopContainerInstance())
      }
    }

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      webappPort := 8080,
      webappStart := startWebapp.value,
      webappJoin := joinWebapp.value,
      webappStop := stopWebapp.value,
      Global / onLoad := onLoadSetting.value
    )
}
