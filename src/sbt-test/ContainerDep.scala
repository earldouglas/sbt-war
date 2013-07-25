import sbt._
import sbt.Keys._

/**
 * Used by the scripted tests in the group 'webapp-common'.  It is used to
 * specify container dependendencies based on a system property.  This allows
 * our custom scripted configuration to run the same tests for all of the
 * supported servlet containers.
 */
object ContainerDep {
  def containerDepSettings: Seq[Project.Setting[_]] = {
    val container = System.getProperty("plugin.container")
    
    Seq {
      if(container == "jetty6") {
        libraryDependencies ++= Seq(
          "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
          "org.mortbay.jetty" % "jsp-2.0" % "6.1.22" % "container"
        )
      } else if(container == "jetty7") {
        libraryDependencies ++= Seq(
          "org.eclipse.jetty" % "jetty-webapp" % "7.6.11.v20130520" % "container",
          "org.eclipse.jetty" % "jetty-jsp" % "7.6.11.v20130520" % "container"
        )
      } else if(container == "tomcat") {
        libraryDependencies ++= Seq(
          "org.apache.tomcat.embed" % "tomcat-embed-core" % "7.0.22" % "container",
          "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
          "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "7.0.22" % "container",
          "org.eclipse.jdt.core.compiler" % "ecj" % "4.2.1" % "container"
        )
      } else {
        throw new RuntimeException("""|The system property 'plugin.container' is not set to an appropriate servlet container.
                                      |Please specify this property using the SBT flag -D.""".stripMargin)
      }
    }
  }
}