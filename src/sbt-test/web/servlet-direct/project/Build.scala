import sbt._
import WebPlugin._
import Keys._

object MyBuild extends Build {
	lazy val projects = Seq(root)

	lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

	def port = 7125

	lazy val rootSettings = Seq(
		jettyPort := port,
		jettyScanInterval := 60,
		libraryDependencies ++= libDeps,
		getPage := getPageTask,
		checkPage <<= checkPageTask
	)

	def libDeps =
		if(new File("jetty7") exists)
			jetty7Dependencies	
		else
			jetty6Dependencies

	def jetty6Dependencies =
		Seq("javax.servlet" % "servlet-api" % "2.5" % "provided",
				"org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty")
	def jetty7Dependencies = Seq(
		"javax.servlet" % "servlet-api" % "2.5" % "provided",
		"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
		"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty")

	def indexURL = new java.net.URL("http://localhost:" + port)
	def indexFile = new java.io.File("index.html")

	lazy val getPage = TaskKey[Unit]("get-page")
	
	def getPageTask {
		indexURL #> indexFile !
	}

	lazy val checkPage = InputKey[Unit]("check-page")
	
	def checkPageTask = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
		(getPage, result) map {
			(gp, args) =>
			checkHelloWorld(args.mkString(" ")) foreach error
		}				
	}

	private def checkHelloWorld(checkString: String) =
	{
		val value = IO.read(indexFile)
		if(value.contains(checkString)) None else Some("index.html did not contain '" + checkString + "' :\n" +value)
	}
}
