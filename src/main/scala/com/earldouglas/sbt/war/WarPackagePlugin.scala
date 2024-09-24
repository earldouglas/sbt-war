package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys.artifact
import sbt.Keys.moduleName
import sbt.Keys.{`package` => pkg}
import sbt._

/** Identifies the files that compose the .war file (resources, .class
  * files in the classes/ directory, and .jar files in the lib/
  * directory).
  *
  * Also configures the .war file as an sbt package artifact.
  *
  * This is also used by other user-facing plugins (WebappRunnerPlugin).
  */
object WarPackagePlugin extends AutoPlugin {

  override def requires = WebappComponentsPlugin

  val warContents: Initialize[Task[Seq[(File, String)]]] =
    Def.task {

      import WebappComponentsPlugin.autoImport._

      WarPackage.getWarContents(
        webappResources = webappResources.value,
        webappClasses = webappClasses.value,
        webappLib = webappLib.value
      )
    }

  override lazy val projectSettings: Seq[Setting[_]] = {

    val packageTaskSettings: Seq[Setting[_]] =
      Defaults.packageTaskSettings(pkg, warContents)

    val packageArtifactSetting: Setting[_] =
      pkg / artifact := Artifact(moduleName.value, "war", "war")

    val artifactSettings: Seq[Setting[_]] =
      addArtifact(Compile / pkg / artifact, pkg)

    Seq(
      packageTaskSettings,
      Seq(packageArtifactSetting),
      artifactSettings
    ).flatten
  }
}
