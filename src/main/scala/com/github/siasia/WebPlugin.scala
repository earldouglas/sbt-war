package com.github.siasia

import sbt._
import Keys._
import PluginKeys._

object WebPlugin extends Plugin {
	lazy val container = Container("container")
	def webSettings =
		container.settings ++
		WebappPlugin.webappSettings ++
		Seq(
			apps in container.Configuration <<= (deployment in Runtime) map {
				d =>
				Seq("/" -> d)}
		)
}
