import sbt._
import com.earldouglas.xsbtwebplugin._
import WebPlugin._
import PluginKeys._
import Keys._

object MyBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

  def Conf = config("container")

  def jettyPort = 7127

  lazy val rootSettings =  Seq(
    port in Conf := jettyPort,
    env in Compile := Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile),
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container",
      "org.eclipse.jetty" % "jetty-plus" % "7.3.0.v20110203" % "container",
      "javax.servlet" % "servlet-api" % "2.5" % "provided"
    ),
    getPage := getPageTask,
    checkPage <<= checkPageTask
  )

  def indexURL = new java.net.URL("http://localhost:" + jettyPort)
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
