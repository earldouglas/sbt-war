package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.taskKey
import sbt.Keys._
import sbt._

/** Identifies the files that compose the webapp (resources, .class
  * files, and .jar files). This is used by user-facing plugins
  * (WarPlugin and WebappRunnerPlugin).
  */
object WebappComponentsPlugin extends AutoPlugin {

  object autoImport {

    lazy val webappResources =
      taskKey[Map[File, String]]("webapp resources")

    lazy val webappClasses =
      taskKey[Map[File, String]]("webapp classes")

    lazy val webappLib =
      taskKey[Map[File, String]]("webapp lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  private val webappResourcesDir: Initialize[File] =
    Def.setting((Compile / sourceDirectory).value / "webapp")

  val webappResourcesSetting: Initialize[Map[File, String]] =
    Def.setting(WebappComponents.getResources(webappResourcesDir.value))

  private val classpathFiles: Initialize[Task[Seq[File]]] =
    Def.task((Runtime / fullClasspath).value.files)

  val webappClassesTask: Initialize[Task[Map[File, String]]] =
    Def.task(WebappComponents.getClasses(classpathFiles.value))

  val webappLibTask: Initialize[Task[Map[File, String]]] =
    Def.task(WebappComponents.getLib(classpathFiles.value))

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      webappResources := webappResourcesSetting.value,
      webappClasses := webappClassesTask.value,
      webappLib := webappLibTask.value
    )
}
