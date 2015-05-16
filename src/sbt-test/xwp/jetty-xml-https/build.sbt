name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerConfigFile := Some(file("etc/jetty.xml"))