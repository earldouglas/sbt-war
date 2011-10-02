package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
	
object WebappPlugin extends Plugin {
	def webappSettings0 = Seq(
		webappResources <<= sourceDirectory(sd => Seq(sd / "webapp")),
		deployment <<= (webappResources, fullClasspath) map {
			(rs, cp) =>
			Deployment(rs, cp.map(_.data))
		}
	)
	def webappSettings = inConfig(Runtime)(webappSettings0)
}
