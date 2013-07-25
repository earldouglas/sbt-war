import sbt._
import com.earldouglas.xsbtwebplugin._
import WebPlugin._
import PluginKeys._
import Keys._

object MyBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

  def jettyPort = 7123

  def Conf = config("container")

  lazy val rootSettings = Seq(
    port in Conf := jettyPort,
    scanInterval in Compile := 60,
    libraryDependencies ++= libDeps,
    getPage := getPageTask,
    checkPage <<= checkPageTask
  )

  def libDeps =
    if(new File("jetty7.2") exists)
      jetty72Dependencies
    else if(new File("jetty7.3") exists)
      jetty73Dependencies  
    else
      jetty6Dependencies

  def jetty6Dependencies =
    Seq("javax.servlet" % "servlet-api" % "2.5" % "provided",
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container")
  def jetty72Dependencies =
    Seq("javax.servlet" % "servlet-api" % "2.5" % "provided",
    "org.eclipse.jetty" % "jetty-webapp" % "7.2.2.v20101205" % "container")
  def jetty73Dependencies =
    Seq("javax.servlet" % "servlet-api" % "2.5" % "provided",
    "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container")

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
