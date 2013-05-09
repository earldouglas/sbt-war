import sbt._
import Keys._

object PluginDef extends Build {
	lazy val root = Project("plugins", file(".")) settings(
		libraryDependencies <++= sbtVersion(v => Seq(
			"org.scala-sbt" % "scripted-plugin" % v
		))
	)
}
