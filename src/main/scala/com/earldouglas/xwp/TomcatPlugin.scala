package com.earldouglas.xwp

import sbt._

object TomcatPlugin extends AutoPlugin {

  object autoImport {
    lazy val Tomcat = config("tomcat").hide
  }

  import ContainerPlugin.autoImport._
  import autoImport._

  override def requires = ContainerPlugin

  override val projectConfigurations = Seq(Tomcat)

  val webappRunner = "com.heroku" % "webapp-runner" % "9.0.68.1"

  override lazy val projectSettings =
    ContainerPlugin.containerSettings(Tomcat) ++
      inConfig(Tomcat)(
        Seq(
          containerLibs := Seq(webappRunner.intransitive()),
          containerMain := "webapp.runner.launch.Main"
        )
      )
}
