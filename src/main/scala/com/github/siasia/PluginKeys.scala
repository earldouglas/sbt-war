package com.github.siasia

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
	lazy val port = SettingKey[Int]("port")
	lazy val apps = TaskKey[Seq[(String, Deployment)]]("apps")
	lazy val start = TaskKey[Unit]("start")
	lazy val discoveredContexts = TaskKey[Seq[String]]("discovered-contexts")
	lazy val reload = InputKey[Unit]("reload")
	lazy val stop = TaskKey[Unit]("stop")
	lazy val customConfiguration = SettingKey[Boolean]("custom-configuration")
	lazy val configurationFiles = SettingKey[Seq[File]]("configuration-files")
	lazy val configurationXml = SettingKey[NodeSeq]("configuration-xml")
	lazy val webappResources = SettingKey[Seq[File]]("webapp-resources")
	lazy val packageJar = TaskKey[File]("package-jar")
	lazy val scanDirectories = SettingKey[Seq[File]]("scan-directories")
	lazy val scanInterval = SettingKey[Int]("scan-interval")
	lazy val env = SettingKey[Option[File]]("env")
	lazy val deployment = TaskKey[Deployment]("deployment")
}
