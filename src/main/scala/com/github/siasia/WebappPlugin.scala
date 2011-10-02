package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
	
object WebappPlugin extends Plugin {
	def webappSettings0 = Seq(
		webappResources <<= sourceDirectory(sd => Seq(sd / "webapp")),
		scanDirectories <<= classDirectory(Seq(_)),
		scanInterval := 3,
		deployment <<= (webappResources, fullClasspath, scanDirectories, scanInterval) map {
			(rs, cp, sd, si) =>
			Deployment(rs, cp.map(_.data), sd, si)
		}
	)
	def webappSettings = inConfig(Runtime)(webappSettings0)
}
