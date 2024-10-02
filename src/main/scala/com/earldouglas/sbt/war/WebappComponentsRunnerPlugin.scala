package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys._
import sbt._

import java.util.concurrent.atomic.AtomicReference

/** Launches the webapp composed of the resources, classes, and
  * libraries specified by WebappComponentsPlugin.
  */
object WebappComponentsRunnerPlugin extends AutoPlugin {

  object autoImport {
    lazy val Webapp = config("webapp").hide
    lazy val webappPort = settingKey[Int]("webapp container port")
    lazy val webappStart = taskKey[Unit]("start webapp container")
    lazy val webappJoin = taskKey[Unit]("join webapp container")
    lazy val webappStop = taskKey[Unit]("stop webapp container")
  }

  import autoImport._

  override val requires: Plugins =
    WebappComponentsPlugin

  private lazy val containerInstance =
    new AtomicReference[Option[WebappComponentsRunner]](None)

  override val projectSettings: Seq[Setting[_]] = {

    def stopContainerInstance(): Unit =
      containerInstance.getAndSet(None).foreach(_.stop())

    val startWebapp: Initialize[Task[Unit]] =
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

    val joinWebapp: Initialize[Task[Unit]] =
      Def.task(containerInstance.get.foreach(_.join()))

    val stopWebapp: Initialize[Task[Unit]] =
      Def.task(stopContainerInstance())

    val onLoadSetting: Initialize[State => State] =
      Def.setting {
        (Global / onLoad).value compose { state: State =>
          state.addExitHook(stopContainerInstance())
        }
      }

    Seq(
      webappPort := 8080,
      webappStart := startWebapp.value,
      webappJoin := joinWebapp.value,
      webappStop := stopWebapp.value,
      Global / onLoad := onLoadSetting.value
    )
  }
}
