package com.earldouglas.xwp

import sbt._

object XwpPlugin extends Plugin
                    with WebappPlugin
                    with WarPlugin
                    with ContainerPlugin {

  def jetty(
      port: Int = 8080
    , runner: Option[ModuleID] = 
        Some("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % 
             "container" intransitive())
    , main: String = "org.eclipse.jetty.runner.Runner"
  ): Seq[Setting[_]] =
    runnerContainer(port, runner, main) ++ warSettings ++ webappSettings

  def tomcat(
      port: Int = 8080
    , runner: Option[ModuleID] = 
        Some("com.github.jsimone" % "webapp-runner" % "7.0.34.1" % "container"
             intransitive())
    , main: String = "webapp.runner.launch.Main"
  ): Seq[Setting[_]] =
    runnerContainer(port, runner, main) ++ warSettings ++ webappSettings

}
