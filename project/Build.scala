import sbt.{Node => _, _}

import Keys._
import scala.xml._

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
				import org.mortbay.xml.XmlConfiguration
				import org.mortbay.jetty.plus.webapp.EnvConfiguration
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
				import org.eclipse.jetty.webapp.WebAppContext
				import org.eclipse.jetty.util.{Scanner => JScanner}
				import org.eclipse.jetty.util.log.{Log, Logger => JLogger}
				import org.eclipse.jetty.util.resource.ResourceCollection
				import org.eclipse.jetty.xml.XmlConfiguration
				import org.eclipse.jetty.plus.webapp.EnvConfiguration
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

	def pomPostProcessTask(scalaVersion: String, sbtVersion: String)(node: Node) = node match {
		case xml: Elem =>
			val children = Seq(
				<url>http://github.com/siasia/xsbt-web-plugin</url>,
				<licenses>
					<license>
						<name>BSD 3-Clause</name>
						<url>https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE</url>
						<distribution>repo</distribution>
					</license>
				</licenses>,
				<scm>
					<connection>scm:git:git@github.com:siasia/xsbt-web-plugin.git</connection>
					<developerConnection>scm:git:git@github.com:siasia/xsbt-web-plugin.git</developerConnection>
					<url>git@github.com:siasia/xsbt-web-plugin.git</url>
				</scm>,
				<developers>
					<developer>
						<id>siasia</id>
						<name>Artyom Olshevskiy</name>
						<email>siasiamail@gmail.com</email>
					</developer>
				</developers>,
				<parent>
					<groupId>org.sonatype.oss</groupId>
					<artifactId>oss-parent</artifactId>
					<version>7</version>
				</parent>
			)
		def massagePomChild(child: Node) = child match {
			case Elem(_, "properties", _, _, _*) =>
				None
			case e @ Elem(_, "artifactId", _, _, _*) =>
				Some(<artifactId>{e.text+"_"+scalaVersion}</artifactId>)
			case e @ Elem(_, "version", _, _, _*) =>
				Some(<version>{sbtVersion+"-"+e.text}</version>)
			case _ =>	Some(child)
		}
		xml.copy(child = xml.child.flatMap(massagePomChild) ++ children)
	}
	
	
	def rootSettings = Seq(
		sbtPlugin := true,
		organization := "com.github.siasia",
		name := "xsbt-web-plugin",
		version := "0.2.0",		
		publishMavenStyle := true,
		publishTo <<= (version) {
			version: String =>
			val ossSonatype = "https://oss.sonatype.org/"
			if (version.trim.endsWith("SNAPSHOT"))
				Some("snapshots" at ossSonatype + "content/repositories/snapshots") 
			else None
		},
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
		pomIncludeRepository := ((_) => false),
		pomPostProcess <<= (scalaVersion, sbtVersion) (pomPostProcessTask(_, _) _),
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
		sourceGenerators in Compile <+= generateJettyRunners.task
	)
	
	lazy val root = Project("root", file(".")) settings(rootSettings :_*)
}
