import sbt._

import Keys._
import Project.Initialize

object WebBuild extends Build {
	lazy val projects = Seq(root, web)

	val generateJettyRun = TaskKey[Seq[File]]("generate-jetty-run")

	lazy val root = Project("root", file("."), settings = rootSettings ) aggregate(web) dependsOn(web)

	lazy val web = Project("web", file("web"), settings = webSettings )
	
	lazy val rootSettings = Defaults.defaultSettings ++	
	ScriptedPlugin.scriptedSettings ++
	rootOnlySettings ++
	sharedSettings
	
	lazy val rootOnlySettings = Seq(
		sbtPlugin := true,
		ScriptedPlugin.scriptedBufferLog := false,
		resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns),
		publishLocal <<= (publishLocal in web, publishLocal) map {(_, p) => p },
		organization := "com.github.siasia",
		name := "xsbt-web-plugin",
		libraryDependencies <<= (libraryDependencies, appConfiguration) {
			(deps, app) =>
			val version = app.provider.id.version
			deps ++ Seq(
				"org.scala-tools.sbt" %% "classpath" % version
			)
		}
	)

	lazy val webSettings = Defaults.defaultSettings ++ webOnlySettings ++ sharedSettings

	lazy val webOnlySettings = Seq(
		organization := "com.github.siasia.sbt",
		name := "web-app",
		libraryDependencies <<= (libraryDependencies, appConfiguration) {
			(deps, app) =>
			val version = app.provider.id.version
			deps ++ Seq(
				"org.scala-tools.sbt" %% "io" % version,
				"org.scala-tools.sbt" %% "logging" % version,
				"org.scala-tools.sbt" %% "classpath" % version,
				"org.scala-tools.sbt" %% "process" % version,
				"org.mortbay.jetty" % "jetty" % "6.1.22" % "optional",
				"org.mortbay.jetty" % "jetty-plus" % "6.1.22" % "optional",
				"org.eclipse.jetty" % "jetty-server" % "7.3.0.v20110203" % "optional",
				"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "optional",
				"org.eclipse.jetty" % "jetty-plus" % "7.3.0.v20110203" % "optional"
			)
		},
		generateJettyRun <<= generateJettyRunTask,
		sources in Compile <<= (generateJettyRun, sources in Compile) map {(generated, sources) => (sources ++ generated) distinct}
	)

	lazy val sharedSettings = Seq(
		version <<= appConfiguration(_.provider.id.version),
		publishMavenStyle := true,
		publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "siasia.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern))),
		scalacOptions += "-deprecation"
	)

	def generateJettyRunTask: Initialize[Task[Seq[File]]] = (scalaSource in Compile) map {
		(srcDir) =>
  	Seq("6", "7").map{
			n =>
			val target = srcDir / ("LazyJettyRun" + n + ".scala")
			generateJettyRun(srcDir / "LazyJettyRun.scala.templ", target, n, srcDir / ("jetty" + n + ".imports"), srcDir / ("jetty" + n + ".filesChanged.type"))
			target
		}
	}

	def generateJettyRun(in: File, out: File, version: String, importsPath: File, filesChangedTypePath: File) {
		val template = IO.read(in asFile)
		val imports = IO.read(importsPath asFile)
		val filesChanged = IO.read(filesChangedTypePath asFile)
		IO.write(out asFile, processJettyTemplate(template, version, imports, filesChanged))
	}
	def processJettyTemplate(template: String, version: String, imports: String, filesChanged: String): String =
		template.replaceAll("""\Q${jetty.version}\E""", version).replaceAll("""\Q${jetty.imports}\E""", imports).replaceAll("""\Q${filesChanged.type}\E""", filesChanged)	
}
