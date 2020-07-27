scalaVersion := "2.13.3"

libraryDependencies += "net.liftweb" %% "lift-webkit" % "3.2.0"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

enablePlugins(JettyPlugin)
