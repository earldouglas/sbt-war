package com.github.siasia

import sbt._

case class Deployment(
	webappResources: Seq[File],
	classpath: Seq[File],
	scanDirectories: Seq[File],
	scanInterval: Int
)

object Defaults {
	val conf = Runtime
}
	
object PluginKeys {
	lazy val port = SettingKey[Int]("port")
	lazy val apps = TaskKey[Seq[(String, Deployment)]]("apps")
	lazy val start = TaskKey[Unit]("start")
	lazy val discoveredContexts = TaskKey[Seq[String]]("discovered-contexts")
	lazy val reload = InputKey[Unit]("reload")
	lazy val stop = TaskKey[Unit]("stop")
	lazy val webappResources = SettingKey[Seq[File]]("webapp-resources")
	lazy val scanDirectories = SettingKey[Seq[File]]("scan-directories")
	lazy val scanInterval = SettingKey[Int]("scan-interval")
	lazy val packageWar = TaskKey[File]("package-war")
	lazy val deployment = TaskKey[Deployment]("deployment")
}
