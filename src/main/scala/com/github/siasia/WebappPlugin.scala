package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
import WarPlugin.warSettings0
	
object WebappPlugin extends Plugin {
	def webappSettings0 = warSettings0 ++ Seq(
		scanDirectories <<= classDirectory(Seq(_)),
		scanInterval := 3,
		env := None,
		deployment <<= (webappResources, fullClasspath, scanDirectories, scanInterval, env) map {
			(rs, cp, sd, si, env) =>
			Deployment(rs, cp.map(_.data), sd, si, env)
		}
	)
	def webappSettings = inConfig(DefaultConf)(webappSettings0)
}
