import sbt.{Node => _, _}

import Keys._
import scala.xml._
import std.TaskExtra._
import com.github.siasia._
import SonatypePlugin._

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
				import org.mortbay.jetty.nio.SelectChannelConnector
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
				"imports" -> """
				import org.eclipse.jetty.server.{Server, Handler}
				import org.eclipse.jetty.server.handler.ContextHandlerCollection
				import org.eclipse.jetty.server.nio.SelectChannelConnector
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

	def sharedSettings = sonatypeSettings ++ Seq(
		projectID <<= (organization,moduleName,version,artifacts,crossPaths){ (org,module,version,as,crossEnabled) =>
			ModuleID(org, module, version).cross(crossEnabled).artifacts(as : _*)
		},
		organization := "com.github.siasia",
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
		pomUrl := "http://github.com/siasia/xsbt-web-plugin",
		licenses := Seq(
			"BSD 3-Clause" -> new URL("https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE")
		),
		scm := (
			"scm:git:git@github.com:siasia/xsbt-web-plugin.git",
			"scm:git:git@github.com:siasia/xsbt-web-plugin.git",
			"git@github.com:siasia/xsbt-web-plugin.git"
		),
		developers := Seq((
			"siasia",
			"Artyom Olshevskiy",
			"siasiamail@gmail.com"
		))		
	)

	def appendedSettings = Seq(
		version <<= (sbtVersion, version)(_ + "-" + _)
	)

	def rootSettings: Seq[Setting[_]] = sharedSettings ++ Seq(
		sbtPlugin := true,
		name := "xsbt-web-plugin",
		version := "0.2.10",
		libraryDependencies ++= Seq(
			"org.mortbay.jetty" % "jetty" % "6.1.22" % "optional",
			"org.mortbay.jetty" % "jetty-plus" % "6.1.22" % "optional",
			"org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % "optional",
			"org.eclipse.jetty" % "jetty-plus" % "7.5.1.v20110908" % "optional"
		),
		templatesDirectory <<= (sourceDirectory in Runtime)(_ / "templates"),
		generateJettyRunners <<= (templatesDirectory, target) map {
			(templateDir, target) =>
			generateJettyRunnersTask((templateDir ** "*.scala").get, target)
		},
		sourceGenerators in Compile <+= generateJettyRunners.task,
		publishLocal <<= (publishLocal in commons, publishLocal) map ((_, p) => p),
		scalacOptions += "-deprecation"
	) ++ appendedSettings

	def commonsSettings = sharedSettings ++ Seq(
		name := "plugin-commons",
		version := "0.1",
		libraryDependencies <++= (sbtVersion) {
			(v) => Seq(
				"org.scala-tools.sbt" %% "classpath" % v % "provided"
			)}
	) ++ appendedSettings
	
	lazy val root = Project("root", file(".")) settings(rootSettings :_*) dependsOn(commons)
	lazy val commons = Project("commons", file("commons")) settings(commonsSettings :_*)
}
