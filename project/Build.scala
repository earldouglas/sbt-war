import sbt.{Node => _, _}

import Keys._
import scala.xml._
import ScriptedPlugin._
import std.TaskExtra._

object PluginBuild extends Build {
	val templatesDirectory = SettingKey[File]("templates-directory")
	val generateJettyRunners = TaskKey[Seq[File]]("generate-jetty-runners")

	def generateJettyRunnersTask(templates: Seq[File], target: File) = {
		val data = Seq(
			Map(
				"version" -> "6",
				"sslConnectorClass" -> "SslSocketConnector",
				"imports" -> """                                
				import org.mortbay.jetty.{Server, Handler}
				import org.mortbay.jetty.handler.ContextHandlerCollection
				import org.mortbay.jetty.nio.SelectChannelConnector
				import org.mortbay.jetty.security.SslSocketConnector
				import org.mortbay.jetty.webapp.{WebAppClassLoader, WebAppContext, WebInfConfiguration, Configuration, JettyWebXmlConfiguration, TagLibConfiguration, WebXmlConfiguration}
				import org.mortbay.util.{Scanner => JScanner}
				import org.mortbay.log.{Log, Logger => JLogger}
				import org.mortbay.resource.ResourceCollection
				import org.mortbay.xml.XmlConfiguration
				import org.mortbay.jetty.plus.webapp.{EnvConfiguration, Configuration=>PlusConfiguration}
				""",
				"filesChanged.type" -> "_",
				"envConfig.init" -> """
				setWebAppContext(context)
				configureWebApp()
				"""
			),
			Map(
				"version" -> "7",
				"sslConnectorClass" -> "SslSelectChannelConnector",
				"imports" -> """
				import org.eclipse.jetty.server.{Server, Handler}
				import org.eclipse.jetty.server.handler.ContextHandlerCollection
				import org.eclipse.jetty.server.nio.SelectChannelConnector
				import org.eclipse.jetty.server.ssl.SslSelectChannelConnector                
				import org.eclipse.jetty.webapp.{WebAppClassLoader, WebAppContext, WebInfConfiguration, Configuration, FragmentConfiguration, JettyWebXmlConfiguration, TagLibConfiguration, WebXmlConfiguration}
				import org.eclipse.jetty.util.{Scanner => JScanner}
				import org.eclipse.jetty.util.log.{Log, Logger => JLogger}
				import org.eclipse.jetty.util.resource.ResourceCollection
				import org.eclipse.jetty.xml.XmlConfiguration
				import org.eclipse.jetty.plus.webapp.{EnvConfiguration, PlusConfiguration}
				""",
				"filesChanged.type" -> "String",
				"envConfig.init" -> """
				configure(context)
				"""
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

	def sharedSettings = Seq[Setting[_]](
		organization := "com.earldouglas",
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
	)

	def rootSettings: Seq[Setting[_]] = sharedSettings ++ scriptedSettings ++ Seq(
		sbtPlugin := true,
		name := "xsbt-web-plugin",
		version := "0.2.12-SNAPSHOT",
    crossScalaVersions := Seq("2.9.2", "2.10.0", "2.10.1"),
		libraryDependencies ++= Seq(
			"org.mortbay.jetty" % "jetty" % "6.1.22" % "optional",
			"org.mortbay.jetty" % "jetty-plus" % "6.1.22" % "optional",
			"org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % "optional",
			"org.eclipse.jetty" % "jetty-plus" % "7.5.1.v20110908" % "optional",
			"org.apache.tomcat.embed" % "tomcat-embed-core" % "7.0.22" % "optional",
			"org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "optional",
			"org.apache.tomcat.embed" % "tomcat-embed-jasper" % "7.0.22" % "optional"
		),
		templatesDirectory <<= (sourceDirectory in Runtime)(_ / "templates"),
		generateJettyRunners <<= (templatesDirectory, target) map {
			(templateDir, target) =>
			generateJettyRunnersTask((templateDir ** "*.scala").get, target)
		},
		sourceGenerators in Compile <+= generateJettyRunners.task,
		scalacOptions += "-deprecation",
		scriptedBufferLog := false
	)

	lazy val root = Project("root", file(".")) settings(rootSettings :_*)
}
