import sbt._
import WebPlugin._
import Keys._
import Project.Initialize

object MyBuild extends Build {
	lazy val projects = Seq(root, sub)

	private val indexFile = SettingKey[File]("index-file")
	private val indexUrl = SettingKey[java.net.URL]("index-page")
	
	lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ sharedSettings ++ Seq(
		jettyPort := 7122
	))
	lazy val sub = Project("sub", file("sub"), settings = Defaults.defaultSettings ++ webSettings ++ sharedSettings ++ Seq(
		jettyPort := 7121
	))

	lazy val sharedSettings = Seq(
		jettyScanInterval := 60,
		libraryDependencies ++= libDeps,
		indexFile <<= baseDirectory / "index.html",
		indexUrl <<= jettyPort(port => new java.net.URL("http://localhost:" + port)),
		getPage <<= getPageTask,
		checkPage <<= checkPageTask
	)

	def libDeps =
			jettyDependencies	

	def jettyDependencies =
		Seq("javax.servlet" % "servlet-api" % "2.5" % "provided",
		"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty")

	lazy val getPage = TaskKey[File]("get-page")
	
	def getPageTask: Initialize[Task[File]] = (indexUrl, indexFile) map {
		(indexUrl, indexFile) =>
		indexUrl #> indexFile !;
		indexFile
	}

	lazy val checkPage = InputKey[Unit]("check-page")
	
	def checkPageTask = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
		(getPage, result) map {
			(gp, args) =>
			checkHelloWorld(gp, args.mkString(" ")) foreach error
		}				
	}

	private def checkHelloWorld(indexFile: File, checkString: String) =
	{
		val value = IO.read(indexFile)
		if(value.contains(checkString)) None else Some("index.html did not contain '" + checkString + "' :\n" +value)
	}
}
