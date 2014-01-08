import sbt._
import Keys._

object XsbtWebPlugin extends Build {

  lazy val root =
    Project(id = "xsbt-web-plugin", base = file("."),
            settings = Defaults.defaultSettings ++
                       jetty6Settings ++ jetty7Settings ++ jetty9Settings ++ tomcat7Settings ++
                       Seq(
                         packageConfiguration in (Compile, packageBin) <<=
                           (
                               packageConfiguration in (Compile, packageBin)
                             , packageConfiguration in (Jetty6, packageBin)
                             , packageConfiguration in (Jetty7, packageBin)
                             , packageConfiguration in (Jetty9, packageBin)
                             , packageConfiguration in (Tomcat7, packageBin)
                           ) map { (c,j6,j7,j9,t7) =>
                             val sources = c.sources ++ j6.sources ++ j7.sources ++ j9.sources ++ t7.sources
                             new Package.Configuration(sources, c.jar, c.options)
                           }
                       )
    )

  lazy val Jetty6 = config("jetty-6").extend(Runtime, Provided)
  lazy val jetty6Settings: Seq[Setting[_]] = 
    inConfig(Jetty6)(Defaults.configSettings) ++
    Seq(
        libraryDependencies ++= Seq(
            "org.mortbay.jetty" % "jetty"      % "6.1.22" % Jetty6
          , "org.mortbay.jetty" % "jetty-plus" % "6.1.22" % Jetty6
        )
      , ivyConfigurations += Jetty6
    )

  lazy val Jetty7 = config("jetty-7").extend(Runtime, Provided)
  lazy val jetty7Settings: Seq[Setting[_]] = 
    inConfig(Jetty7)(Defaults.configSettings) ++
    Seq(
        libraryDependencies ++= Seq(
            "org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % Jetty7
          , "org.eclipse.jetty" % "jetty-plus"   % "7.5.1.v20110908" % Jetty7
        )
      , ivyConfigurations += Jetty7
    )

  lazy val Jetty9 = config("jetty-9").extend(Runtime, Provided)
  lazy val jetty9Settings: Seq[Setting[_]] = 
    inConfig(Jetty9)(Defaults.configSettings) ++
    Seq(
        libraryDependencies ++= Seq(
            "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % Jetty9
          , "org.eclipse.jetty" % "jetty-annotations"   % "9.1.0.v20131115" % Jetty9
          , "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % Jetty9
        )
      , ivyConfigurations += Jetty9
    )

  lazy val Tomcat7 = config("tomcat-7").extend(Runtime, Provided)
  lazy val tomcat7Settings: Seq[Setting[_]] = 
    inConfig(Tomcat7)(Defaults.configSettings) ++
    Seq(
        libraryDependencies ++= Seq(
            "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % Tomcat7
          , "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % Tomcat7
          , "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % Tomcat7
        )
      , ivyConfigurations += Tomcat7
    )

}
