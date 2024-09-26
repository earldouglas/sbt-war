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
      taskKey[Map[String, File]]("webapp resources")

    lazy val webappClasses =
      taskKey[Map[String, File]]("webapp classes")

    lazy val webappLib =
      taskKey[Map[String, File]]("webapp lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override def projectSettings: Seq[Setting[_]] = {

    val webappResourcesDir: Initialize[File] =
      Def.setting((Compile / sourceDirectory).value / "webapp")

    val webappResourcesTask: Initialize[Task[Map[String, File]]] =
      Def.task(WebappComponents.getResources(webappResourcesDir.value))

    val classpathFiles: Initialize[Task[Seq[File]]] =
      Def.task((Runtime / fullClasspath).value.files)

    val webappClassesTask: Initialize[Task[Map[String, File]]] =
      Def.task(WebappComponents.getClasses(classpathFiles.value))

    val webappLibTask: Initialize[Task[Map[String, File]]] =
      Def.task(WebappComponents.getLib(classpathFiles.value))

    Seq(
      webappResources := webappResourcesTask.value,
      webappClasses := webappClassesTask.value,
      webappLib := webappLibTask.value
    )
  }

  lazy val webappContents: Initialize[Task[Map[String, File]]] =
    Def.task {
      webappResources.value ++
        webappClasses.value ++
        webappLib.value
    }
}
