package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.taskKey
import sbt.Keys._
import sbt._

/** Identifies the files that compose the webapp (resources, .class
  * files, and .jar files). This is used by user-facing plugins
  * (WarPlugin and WebappRunnerPlugin).
  *
  * Webapp components are managed as three sets of mappings:
  *
  *   - webappResources: All the static HTML, CSS, JS, images, etc.
  *     files to be served by the application. Also, optionally, the
  *     WEB-INF/web.xml deployment descriptor.
  *   - webappClasses: All of the classes, etc. on the classpath to be
  *     copied into the WEB-INF/classes directory.
  *   - webappLib: All of the .jar files to be copied into the
  *     WEB-INF/lib directory.
  *
  * These mappings each have the type Map[String, File], where the key
  * is the relative path within the .war file (e.g.
  * WEB-INF/classes/Foo.class), and the value is the location of the
  * file to be copied there (e.g. target/classes/Foo.class).
  */
object WebappComponentsPlugin extends AutoPlugin {

  object autoImport {

    lazy val webappResources: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp resources")

    lazy val webappClasses: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp classes")

    lazy val webappLib: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  lazy val webappContents: Initialize[Task[Map[String, File]]] =
    Def.task {
      webappResources.value ++
        webappClasses.value ++
        webappLib.value
    }

  override val projectSettings: Seq[Setting[_]] = {

    val webappResourcesTask: Initialize[Task[Map[String, File]]] =
      (Compile / sourceDirectory)
        .map(_ / "webapp")
        .map(WebappComponents.getResources)

    val webappClassesTask: Initialize[Task[Map[String, File]]] =
      (Runtime / fullClasspath)
        .map(_.files)
        .map(WebappComponents.getClasses)

    val webappLibTask: Initialize[Task[Map[String, File]]] =
      (Runtime / fullClasspath)
        .map(_.files)
        .map(WebappComponents.getLib)

    Seq(
      webappResources := webappResourcesTask.value,
      webappClasses := webappClassesTask.value,
      webappLib := webappLibTask.value
    )
  }
}
