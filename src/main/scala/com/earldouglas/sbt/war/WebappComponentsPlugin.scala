package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Def.taskKey
import sbt._

/** Identifies the files that compose the webapp (resources, .class
  * files, and .jar files). This is used by user-facing plugins
  * (WarPlugin and WebappRunnerPlugin).
  *
  * Webapp components are managed as three sets of mappings:
  *
  *   - warResources: All the static HTML, CSS, JS, images, etc. files
  *     to be served by the application. Also, optionally, the
  *     WEB-INF/web.xml deployment descriptor.
  *   - warClasses: All of the classes, etc. on the classpath to be
  *     copied into the WEB-INF/classes directory.
  *   - warLib: All of the .jar files to be copied into the WEB-INF/lib
  *     directory.
  *
  * These mappings each have the type Map[String, File], where the key
  * is the relative path within the .war file (e.g.
  * WEB-INF/classes/Foo.class), and the value is the location of the
  * file to be copied there (e.g. target/classes/Foo.class).
  */
object WebappComponentsPlugin extends AutoPlugin {

  object autoImport {

    lazy val warResources: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp resources")

    lazy val warClasses: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp classes")

    lazy val warLib: TaskKey[Map[String, File]] =
      taskKey[Map[String, File]]("webapp lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  lazy val warContents: Initialize[Task[Map[String, File]]] =
    Def.task {
      warResources.value ++
        warClasses.value ++
        warLib.value
    }

  override val projectSettings: Seq[Setting[_]] = {

    val warResourcesTask: Initialize[Task[Map[String, File]]] =
      (Compat.Compile_sourceDirectory)
        .map(_ / "webapp")
        .map(WebappComponents.getResources)

    val warClassesTask: Initialize[Task[Map[String, File]]] =
      Compat.classpathFiles
        .map(WebappComponents.getClasses(_))

    val warLibTask: Initialize[Task[Map[String, File]]] =
      Compat.classpathFiles
        .map(WebappComponents.getLib(_))

    Seq(
      warResources := warResourcesTask.value,
      warClasses := warClassesTask.value,
      warLib := warLibTask.value
    )
  }
}
