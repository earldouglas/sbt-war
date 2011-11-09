import sbt._
import Keys._

object PluginDef extends Build {
	lazy val root = Project("plugins", file(".")) settings(
		libraryDependencies <++= sbtVersion(v => Seq(
			"com.github.siasia" %% "oss-sonatype-plugin" % (v+"-0.1")
		))
	)
}
