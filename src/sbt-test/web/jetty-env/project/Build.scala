import sbt._
import com.github.siasia.WebPlugin._
import Keys._

object MyBuild extends Build {
	override def projects = Seq(root)

	lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

	def port = 7127

	lazy val rootSettings = Seq(
		jettyConfFiles <<= jettyConfFiles(_.copy(env = Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile))),
		jettyPort := port,
		libraryDependencies ++= Seq(
			"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
			"org.eclipse.jetty" % "jetty-plus" % "7.3.0.v20110203" % "jetty",
			"javax.servlet" % "servlet-api" % "2.5" % "provided"
		),
		getPage := getPageTask,
		checkPage <<= checkPageTask
	)

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
