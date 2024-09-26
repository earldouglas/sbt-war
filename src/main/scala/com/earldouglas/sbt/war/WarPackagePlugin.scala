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

  override lazy val projectSettings: Seq[Setting[_]] = {

    val packageContents: Initialize[Task[Seq[(java.io.File, String)]]] =
      WebappComponentsPlugin.webappContents
        .map(_.toSeq.map({ case (k, v) => (v, k) }))

    val packageTaskSettings: Seq[Setting[_]] =
      Defaults.packageTaskSettings(pkg, packageContents)

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
