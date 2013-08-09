import sbt._
import com.earldouglas.xsbtwebplugin._
import WebappPlugin._
import PluginKeys._
import Keys._
import Project.Initialize

object MyBuild extends Build {
  override def projects = Seq(root, sub)

  private val indexFile = SettingKey[File]("index-file")
  private val indexUrl = SettingKey[java.net.URL]("index-page")

  lazy val container = Container("container")

  def jettyPort = 7122

  def containerSettings: Seq[Setting[_]] = container.deploy(
    "/root" -> root,
    "/sub" -> sub
  )
  
  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webappSettings ++ sharedSettings ++ Seq(
    indexUrl := new java.net.URL("http://localhost:"+jettyPort+"/root/")
  ) ++ containerSettings ++ Seq(
    port in container.Configuration := jettyPort ,                  
    libraryDependencies ++= Seq(                  
      "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
      "org.mortbay.jetty" % "jsp-2.0" % "6.1.22" % "container"
    )
  ))
  lazy val sub = Project("sub", file("sub"), settings = Defaults.defaultSettings ++ webappSettings ++ sharedSettings ++ Seq(
    indexUrl := new java.net.URL("http://localhost:"+jettyPort+"/sub/")
  ))

  lazy val sharedSettings = Seq(
    scanInterval in Compile := 60,
    libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided",
    indexFile <<= baseDirectory / "index.html",    
    getPage <<= getPageTask,
    checkPage <<= checkPageTask
  )

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
