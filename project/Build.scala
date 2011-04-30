import sbt._

import Keys._
import Project.Initialize

object WebBuild extends Build {
	lazy val projects = Seq(root, web)

	val sharedSettings = Seq(
		version <<= appConfiguration(_.provider.id.version),
		publishMavenStyle := true,
		publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "siasia.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern))),
		resolvers += Resolver.url("Sbt repository", new java.net.URL("http://siasia.github.com/ivy2/"))(Resolver.ivyStylePatterns)
	)

	lazy val root = Project(
		"root", file("."), settings = Defaults.defaultSettings ++
		ScriptedPlugin.scriptedSettings ++ Seq(
		sbtPlugin := true,
		ScriptedPlugin.scriptedBufferLog := false,
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
	) ++ sharedSettings ) aggregate(web) dependsOn(web)

	val generateJettyRun = TaskKey[Seq[File]]("generate-jetty-run")

	def generateJettyRunTask: Initialize[Task[Seq[File]]] = (scalaSource in Compile) map {
		(srcDir) =>
  	Seq("6", "7").map{
			n =>
			val target = srcDir / ("LazyJettyRun" + n + ".scala")
			generateJettyRun(srcDir / "LazyJettyRun.scala.templ", target, n, srcDir / ("jetty" + n + ".imports"), srcDir / ("jetty" + n + ".filesChanged.type"))
			target
		}
	}

	def generateJettyRun(in: Path, out: Path, version: String, importsPath: Path, filesChangedTypePath: Path) {
		val template = IO.read(in asFile)
		val imports = IO.read(importsPath asFile)
		val filesChanged = IO.read(filesChangedTypePath asFile)
		IO.write(out asFile, processJettyTemplate(template, version, imports, filesChanged))
	}
	def processJettyTemplate(template: String, version: String, imports: String, filesChanged: String): String =
		template.replaceAll("""\Q${jetty.version}\E""", version).replaceAll("""\Q${jetty.imports}\E""", imports).replaceAll("""\Q${filesChanged.type}\E""", filesChanged)

	lazy val web = Project("web", file("web"), settings = Defaults.defaultSettings ++ Seq(
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
	) ++ sharedSettings)
}
