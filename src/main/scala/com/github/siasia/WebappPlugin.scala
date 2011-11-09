package com.github.siasia

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
import WarPlugin.warSettings0
	
object WebappPlugin extends Plugin {
	def webappSettings0(classpathConfig: Configuration):Seq[Setting[_]] = warSettings0(classpathConfig) ++ Seq(
		scanDirectories <<= classDirectory(Seq(_)),
		scanInterval := 3,
		env := None,
		deployment <<= (webappResources, fullClasspath in classpathConfig, scanDirectories, scanInterval, env) map {
			(rs, cp, sd, si, env) =>
			Deployment(rs, cp.map(_.data), sd, si, env)
		}
	)
	def webappSettings0:Seq[Setting[_]] = webappSettings0(DefaultClasspathConf)
	def webappSettings(cc: Configuration):Seq[Setting[_]] = inConfig(DefaultConf)(webappSettings0(cc)) ++ WarPlugin.globalWarSettings
	def webappSettings:Seq[Setting[_]] = webappSettings(DefaultClasspathConf)
}
