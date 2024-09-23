package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys._
import sbt._

object WarPackagePlugin extends AutoPlugin {

  import Keys.{`package` => pkg}

  override def requires = WebappComponentsPlugin

  object autoImport {}

  val webappContents: Initialize[Task[Seq[(File, String)]]] =
    Def.task {

      val webappResources: Map[File, String] =
        WebappComponentsPlugin.autoImport.webappResources.value

      val webappClasses: Map[File, String] =
        WebappComponentsPlugin.autoImport.webappClasses.value
          .map { case (k, v) => k -> s"classes/${v}" }

      val webappLib: Map[File, String] =
        WebappComponentsPlugin.autoImport.webappLib.value
          .map { case (k, v) => k -> s"lib/${v}" }

      Seq(
        webappResources,
        webappClasses,
        webappLib
      ).flatten
    }

  override lazy val projectSettings: Seq[Setting[_]] = {

    val packageTaskSettings: Seq[Setting[_]] =
      Defaults.packageTaskSettings(pkg, webappContents)

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
