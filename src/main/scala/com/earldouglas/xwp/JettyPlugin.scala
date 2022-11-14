package com.earldouglas.xwp

import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey

object JettyPlugin extends AutoPlugin {

  object autoImport {
    lazy val Jetty = config("jetty").hide
  }

  import ContainerPlugin.autoImport._
  import autoImport._

  override def requires = ContainerPlugin

  override val projectConfigurations = Seq(Jetty)

  val jettyRunner =
    "org.eclipse.jetty" % "jetty-runner" % "9.4.49.v20220914"

  override lazy val projectSettings =
    ContainerPlugin.containerSettings(Jetty) ++
      inConfig(Jetty)(
        Seq(
          containerLibs := Seq(jettyRunner.intransitive()),
          containerMain := "org.eclipse.jetty.runner.Runner"
        )
      )
}
