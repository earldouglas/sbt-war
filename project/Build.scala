import sbt.{Node => _, _}

import Keys._
import scala.xml._
import ScriptedPlugin._
import std.TaskExtra._
import aether._
import AetherKeys._

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

  def sharedSettings = Aether.aetherSettings ++ Seq(    
    projectID <<= (organization,moduleName,version,artifacts,crossPaths){ (org,module,version,as,crossEnabled) =>
      ModuleID(org, module, version).cross(crossEnabled).artifacts(as : _*)
    },
    deployRepository <<= (version) apply {
      (v: String) => if (v.trim().endsWith("SNAPSHOT")) Resolvers.sonatypeNexusSnapshots else Resolvers.sonatypeNexusStaging
    },
    organization := "com.github.siasia",
    aetherCredentials := {
      val cred = Path.userHome / ".ivy2" / ".credentials"
      if (cred.exists()) Some(Credentials(cred)) else None
    },
    homepage := Some(new URL("http://github.com/siasia/xsbt-web-plugin")),
    startYear := Some(2011),    
    licenses := Seq(
      "BSD 3-Clause" -> new URL("https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE")
    ),
    pomExtra <<= (pomExtra) {(pom) => pom ++ xml.Group(
      <scm>
        <url>http://github.com/arktekk/sbt-aether-deploy</url>
        <connection>scm:git:git://github.com/arktekk/sbt-aether-deploy.git</connection>
        <developerConnection>scm:git:git@github.com:arktekk/sbt-aether-deploy.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <id>siasia</id>
          <name>Artyom Olshevskiy</name>
          <email>siasiamail@gmail.com</email>
        </developer>
      </developers>
    )}    
  )

  def appendedSettings = Seq(
    version <<= (sbtVersion, version)(_ + "-" + _)
  )

  def rootSettings: Seq[Setting[_]] = sharedSettings ++ scriptedSettings ++ Seq(
    sbtPlugin := true,
    name := "xsbt-web-plugin",
    
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
    publish <<= Aether.deployTask.init,
    scalacOptions += "-deprecation",
    scriptedBufferLog := false
  ) ++ appendedSettings

  def commonsSettings = sharedSettings ++ Seq(
    name := "plugin-commons",    
    libraryDependencies <+= (sbtVersion) {
      (v) => v match {
        case v if (v.startsWith("0.12")) => "org.scala-sbt" % "classpath" % v % "provided"
        case "0.11.3" => "org.scala-sbt" %% "classpath" % v % "provided"
        case "0.11.2" => "org.scala-tools.sbt" %% "classpath" % v % "provided"
      }
    }
  ) ++ appendedSettings
  
  lazy val root = Project("root", file(".")) settings(rootSettings :_*) dependsOn(commons)
  lazy val commons = Project("commons", file("commons")) settings(commonsSettings :_*)

  object Resolvers {
    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    val sonatypeNexusStaging = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }
}
