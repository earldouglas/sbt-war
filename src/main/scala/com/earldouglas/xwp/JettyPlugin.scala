package com.earldouglas.xwp

import sbt._

object JettyPlugin extends AutoPlugin {

  object autoImport {
    lazy val Jetty = config("jetty").hide
  }

  import ContainerPlugin.autoImport._
  import autoImport._

  override def requires = ContainerPlugin

  override val projectConfigurations = Seq(Jetty)

  override lazy val projectSettings =
    ContainerPlugin.containerSettings(Jetty) ++
      inConfig(Jetty)(Seq(
        containerLibs := Seq(("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609").intransitive())
      , containerMain := "org.eclipse.jetty.runner.Runner"
      ))
}
