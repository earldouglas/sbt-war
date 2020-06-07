scalaVersion := "2.13.2"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % "test"

enablePlugins(JettyPlugin)
