package com.earldouglas.xwp

import sbt._

object XwpPlugin extends Plugin
with WebappPlugin
with WarPlugin
with ContainerPlugin {

  private def portArg(port: Int): Seq[String] =
    if (port > 0) Seq("--port", port.toString) else Nil

  private def arg(key: String, value: String): Seq[String] =
    if (value.length > 0) Seq(key, value) else Nil

  private val jettyRunner: ModuleID =
    ("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % "container").intransitive

  private val tomcatRunner: ModuleID =
    ("com.github.jsimone" % "webapp-runner" % "7.0.57.2-SNAPSHOT" % "container").intransitive

  def jetty(
             libs: Seq[ModuleID] = Seq(jettyRunner)
             , main: String      = "org.eclipse.jetty.runner.Runner"
             , port: Int         = -1
             , config: String    = ""
             , args: Seq[String] = Nil
             , options: ForkOptions = new ForkOptions
             ): Seq[Setting[_]] = {
    val runnerArgs = Seq(main) ++ portArg(port) ++ arg("--config", config) ++ args
    runnerContainer(libs, runnerArgs, options) ++ warSettings ++ webappSettings
  }

  def tomcat(
              libs: Seq[ModuleID] = Seq(tomcatRunner)
              , main: String        = "webapp.runner.launch.Main"
              , port: Int           = -1
              , args: Seq[String]   = Nil
              , options: ForkOptions = new ForkOptions
              , develop: Boolean = false
              ): Seq[Setting[_]] = {
    val runnerArgs = Seq(main) ++ portArg(port) ++ args
    if(develop) {
      developContainer(libs, runnerArgs, options) ++ warSettings ++ webappSettings
    }
    else {
      runnerContainer(libs, runnerArgs, options) ++ warSettings ++ webappSettings
    }
  }

}
