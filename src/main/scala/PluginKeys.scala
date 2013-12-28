package com.earldouglas.xsbtwebplugin

import sbt._
import scala.xml.NodeSeq

case class Deployment(
  webappResources: Seq[File],
  classpath: Seq[File],
  scanDirectories: Seq[File],
  scanInterval: Int,
  env: Option[File]
)

object PluginKeys extends Plugin {
  lazy val DefaultConf = Compile
  lazy val DefaultClasspathConf = Runtime
  lazy val host = SettingKey[String]("host")
  lazy val port = SettingKey[Int]("port")
  lazy val ssl = TaskKey[Option[(String,Int,String,String,String)]]("ssl")
  lazy val apps = TaskKey[Seq[(String, Deployment)]]("apps")
  lazy val start = TaskKey[Unit]("start")
  lazy val discoveredContexts = TaskKey[Seq[String]]("discovered-contexts")
  lazy val reload = InputKey[Unit]("reload")
  lazy val stop = TaskKey[Unit]("stop")
  lazy val restart = TaskKey[Unit]("restart")
  lazy val customConfiguration = SettingKey[Boolean]("custom-configuration")
  lazy val configurationFiles = SettingKey[Seq[File]]("configuration-files")
  lazy val configurationXml = SettingKey[NodeSeq]("configuration-xml")
  lazy val webappResources = SettingKey[Seq[File]]("webapp-resources")
  lazy val auxCompile = TaskKey[Unit]("aux-compile")
  lazy val warPostProcess = TaskKey[() => Unit]("war-post-process")
  lazy val packageWar = TaskKey[File]("package-war")
  lazy val classesAsJar = SettingKey[Boolean]("classes-as-jar")
  lazy val scanDirectories = SettingKey[Seq[File]]("scan-directories")
  lazy val scanInterval = SettingKey[Int]("scan-interval")
  lazy val env = SettingKey[Option[File]]("env")
  lazy val deployment = TaskKey[Deployment]("deployment")
}
