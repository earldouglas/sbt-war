package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys.artifact
import sbt.Keys.moduleName
import sbt.Keys.{`package` => pkg}
import sbt._

/** Identifies the files that compose the WAR file (resources, .class
  * files in the classes/ directory, and JAR files in the lib/
  * directory).
  *
  * Also configures the WAR file as an sbt package artifact.
  *
  * This is also used by other user-facing plugins (WebappRunnerPlugin).
  */
object WarPackagePlugin extends AutoPlugin {

  override val requires: Plugins =
    WebappComponentsPlugin

  override lazy val projectSettings: Seq[Setting[?]] = {

    // Flip warContents around from (dst -> src) to (src -> dst)
    val packageContents: Initialize[Task[Seq[(java.io.File, String)]]] =
      WebappComponentsPlugin
        .warContents(Runtime)
        .map(_.map(_.swap).toSeq)

    val packageTaskSettings: Seq[Setting[?]] =
      Defaults.packageTaskSettings(pkg, packageContents)

    val packageArtifactSetting: Setting[?] =
      pkg / artifact := Artifact(moduleName.value, "war", "war")

    val artifactSettings: Seq[Setting[?]] =
      addArtifact(Compile / pkg / artifact, pkg)

    Seq(
      packageTaskSettings,
      Seq(packageArtifactSetting),
      artifactSettings
    ).flatten
  }
}
