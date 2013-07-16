import sbt._
import com.earldouglas.xsbtwebplugin._
import WebPlugin._
import PluginKeys._
import Keys._

object MyBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

  def Conf = config("container")

  def jettyPort = 7120

  lazy val rootSettings =  Seq(
    port in Conf := jettyPort,
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-webapp" % "8.1.11.v20130520" % "container",
      "org.eclipse.jetty" % "jetty-plus" % "8.1.11.v20130520" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
    ),
    checkContext := checkContextTask,
    checkJar := checkJarTask
  )

  lazy val checkContext = TaskKey[Unit]("check-context")
  
  def checkContextTask = checkIt(new java.net.URL("http://localhost:" + jettyPort + "/context"),
                                 "This is static1, inside the webapp context\n")

  lazy val checkJar = TaskKey[Unit]("check-jar")
  
  def checkJarTask = checkIt(new java.net.URL("http://localhost:" + jettyPort + "/jar"),
                                 "This is static2, inside a jar file within WEB-INF/lib\n")

  def checkIt(url: java.net.URL, expected: String) = {
    val input = url.openStream
    val output = new java.io.ByteArrayOutputStream
    Iterator.continually(input.read).takeWhile(-1 !=).foreach(output.write)
    input.close
    if (output.toString != expected) error ("unexpected response: " + output.toString)
  }

}
