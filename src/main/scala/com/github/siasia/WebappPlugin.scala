package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
import WarPlugin.warSettings
	
object WebappPlugin extends Plugin {
	def webappSettings0 = Seq(
		scanDirectories <<= classDirectory(Seq(_)),
		scanInterval := 3,
		env := None,
		deployment <<= (webappResources, fullClasspath, scanDirectories, scanInterval, env) map {
			(rs, cp, sd, si, env) =>
			Deployment(rs, cp.map(_.data), sd, si, env)
		}
	)
	def webappSettings = warSettings ++ inConfig(Defaults.conf)(webappSettings0)
}
