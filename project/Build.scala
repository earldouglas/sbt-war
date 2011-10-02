import sbt._

import Keys._

object PluginBuild extends Build {
	val templatesDirectory = SettingKey[File]("templates-directory")
	val generateJettyRunners = TaskKey[Seq[File]]("generate-jetty-runners")

	def generateJettyRunnersTask(templates: Seq[File], target: File) = {
		val data = Seq(
			Map(
				"version" -> "6",
				"imports" -> """
				import org.mortbay.jetty.{Server, Handler}
				import org.mortbay.jetty.handler.ContextHandlerCollection
				import org.mortbay.jetty.webapp.WebAppContext
				import org.mortbay.util.{Scanner => JScanner}
				import org.mortbay.log.{Log, Logger => JLogger}
				import org.mortbay.resource.ResourceCollection
				""",
				"filesChanged.type" -> "_"
			),
			Map(
				"version" -> "7",
				"imports" -> """
				import org.eclipse.jetty.server.{Server, Handler}
				import org.eclipse.jetty.server.handler.ContextHandlerCollection
				import org.eclipse.jetty.webapp.WebAppContext
				import org.eclipse.jetty.util.{Scanner => JScanner}
				import org.eclipse.jetty.util.log.{Log, Logger => JLogger}
				import org.eclipse.jetty.util.resource.ResourceCollection
				""",
				"filesChanged.type" -> "String"
			))
		val root = target / "templates"
		data.zipWithIndex.flatMap {
			case (tpls, i) =>
			val version = root / (i.toString)
			IO.createDirectory(version)
			templates.map {
				template =>
				val content = IO.read(template)
				val result = tpls.foldLeft(content)((content, p) => content.replaceAll("\\$\\{"+p._1+"\\}", p._2))
				val target = version / template.getName()
				IO.write(target, result)
				target
			}
		} toSeq
	}
	
	def rootSettings = Seq(
		sbtPlugin := true,
		organization := "com.github.siasia",
		name := "xsbt-web-plugin",
		version := "0.2-SNAPSHOT",
		publishMavenStyle := true,
		publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "siasia.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern))),
		libraryDependencies ++= Seq(
			"org.mortbay.jetty" % "jetty" % "6.1.22" % "optional",
			"org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % "optional"
		),
		templatesDirectory <<= (sourceDirectory in Runtime)(_ / "templates"),
		generateJettyRunners <<= (templatesDirectory, target) map {
			(templateDir, target) =>
			generateJettyRunnersTask((templateDir ** "*.scala").get, target)
		},
		sourceGenerators in Compile <+= generateJettyRunners.task
	)
	
	lazy val root = Project("root", file(".")) settings(rootSettings :_*)
	override def projects = Seq(root)
}
