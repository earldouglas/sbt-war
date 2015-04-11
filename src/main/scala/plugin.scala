package com.earldouglas.xwp

import sbt._

object Args {

  def port(port: Int): Seq[String] =
    if (port > 0) Seq("--port", port.toString) else Nil

  def arg(key: String, value: String): Seq[String] =
    if (value.nonEmpty) Seq(key, value) else Nil

}

object XwpJetty extends AutoPlugin {

  import XwpPlugin._

  override def requires = plugins.IvyPlugin

  object autoImport {
    val jettyLibs   = settingKey[Seq[ModuleID]]("Jetty modules to launch the webapp")
    val jettyMain   = settingKey[String]("Jetty main class")
    val jettyPort   = settingKey[Int]("port number to be used by Jetty")
    val jettyConfig = settingKey[String]("path of Jetty configuration file")
    val jettyArgs   = settingKey[Seq[String]]("additional Jetty args")
   }
  import autoImport._

  val jettyRunner: ModuleID =
    ("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % Container.name).intransitive

  override lazy val projectSettings: Seq[Setting[_]] =
    XwpPlugin.jetty() ++
    inConfig(Container) {
      Seq(
        jettyLibs   := Seq(jettyRunner),
        jettyMain   := "org.eclipse.jetty.runner.Runner",
        jettyPort   := -1,
        jettyConfig := "",
        jettyArgs   := Nil,
        launchCmd   := Seq(jettyMain.value) ++
                       Args.port(jettyPort.value) ++
                       Args.arg("--config", jettyConfig.value) ++
                       jettyArgs.value :+
                       (Keys.target in webappPrepare).value.getPath
      )
    }

}

object XwpPlugin extends Plugin
                    with WebappPlugin
                    with WarPlugin
                    with ContainerPlugin {

  private val tomcatRunner: ModuleID =
    ("com.github.jsimone" % "webapp-runner" % "7.0.34.1" % Container.name).intransitive

  def jetty(
      libs: Seq[ModuleID] = Seq(XwpJetty.jettyRunner)
    , main: String      = "org.eclipse.jetty.runner.Runner"
    , port: Int         = -1
    , config: String    = ""
    , args: Seq[String] = Nil
    , options: ForkOptions = new ForkOptions
  ): Seq[Setting[_]] = {
    val runnerArgs = Seq(main) ++ Args.port(port) ++ Args.arg("--config", config) ++ args
    runnerContainer(libs, runnerArgs, options) ++ warSettings ++ webappSettings
  }

  def tomcat(
      libs: Seq[ModuleID] = Seq(tomcatRunner)
    , main: String        = "webapp.runner.launch.Main"
    , port: Int           = -1
    , args: Seq[String]   = Nil
    , options: ForkOptions = new ForkOptions
  ): Seq[Setting[_]] = {
    val runnerArgs = Seq(main) ++ Args.port(port) ++ args
    runnerContainer(libs, runnerArgs, options) ++ warSettings ++ webappSettings
  }

}
