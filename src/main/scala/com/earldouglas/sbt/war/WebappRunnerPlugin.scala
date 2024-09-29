package com.earldouglas.sbt.war
import sbt.Def.settingKey
import sbt._

/** Launches a webapp composed of in-place resources, classes, and
  * libraries.
  */
object WebappRunnerPlugin extends AutoPlugin {

  object autoImport {
    lazy val webappRunnerVersion =
      settingKey[String]("webapp-runner version")
  }

  import autoImport._

  override lazy val projectSettings =
    Seq(
      webappRunnerVersion := BuildInfo.webappRunnerVersion
    )
}
