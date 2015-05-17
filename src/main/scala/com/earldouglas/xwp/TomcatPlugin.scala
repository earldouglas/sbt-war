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

  override lazy val projectSettings =
    ContainerPlugin.containerSettings(Tomcat) ++
      inConfig(Tomcat)(Seq(
        containerLibs := Seq(("com.github.jsimone" % "webapp-runner" % "7.0.34.1").intransitive())
      , containerMain := "webapp.runner.launch.Main"
      ))
}
