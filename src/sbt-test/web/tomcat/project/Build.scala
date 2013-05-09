import sbt._
import com.github.siasia._
import WebPlugin._
import PluginKeys._
import Keys._

object MyBuild extends Build {
	override def projects = Seq(root)

	lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

	def Conf = config("container")

	def tomcatPort = 7128

	lazy val rootSettings = Seq(
		port in Conf := tomcatPort,
		scanInterval in Compile := 60,
		libraryDependencies ++= Seq(
      "org.apache.tomcat.embed" % "tomcat-embed-core" % "7.0.22" % "container",
      "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
      "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "7.0.22" % "container",
      "org.eclipse.jdt.core.compiler" % "ecj" % "4.2.1" % "container"
		),
		getPage := getPageTask,
		checkPage <<= checkPageTask
	)

	def indexURL = new java.net.URL("http://localhost:" + tomcatPort)
	def indexFile = new java.io.File("index.html")

	lazy val getPage = TaskKey[Unit]("get-page")
	
	def getPageTask {
		indexURL #> indexFile !
	}

	lazy val checkPage = InputKey[Unit]("check-page")
	
	def checkPageTask = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
		(getPage, result) map {
			(gp, args) =>
			checkHelloWorld(args.mkString(" ")) foreach sys.error
		}				
	}

	private def checkHelloWorld(checkString: String) =
	{
		val value = IO.read(indexFile)
		if(value.contains(checkString)) None else Some("index.html did not contain '" + checkString + "' :\n" +value)
	}
}
