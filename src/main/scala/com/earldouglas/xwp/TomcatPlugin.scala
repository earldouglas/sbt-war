package com.earldouglas.xwp

import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey

object TomcatPlugin extends AutoPlugin {

  object autoImport {
    lazy val Tomcat = config("tomcat").hide
  }

  import ContainerPlugin.autoImport._
  import autoImport._

  override def requires = ContainerPlugin

  override val projectConfigurations = Seq(Tomcat)

  val webappRunner = "com.github.jsimone" % "webapp-runner" % "8.5.9.0"

  override lazy val projectSettings =
    ContainerPlugin.containerSettings(Tomcat) ++
      inConfig(Tomcat)(
        Seq( containerLibs := Seq(webappRunner.intransitive())
           , containerMain := "webapp.runner.launch.Main"
           )
      )
}
