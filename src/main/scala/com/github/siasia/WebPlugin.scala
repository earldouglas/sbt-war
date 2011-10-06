package com.github.siasia

import sbt._
import Keys._
import PluginKeys._

object WebPlugin extends Plugin {
	lazy val container = Container("container")
	def webSettings(conf: Configuration = DefaultConf) =
		container.settings ++
		inConfig(conf)(WebappPlugin.webappSettings0) ++
		Seq(
			apps in container.Configuration <<= (deployment in conf) map {
				d =>
				Seq("/" -> d)}
		)
}
