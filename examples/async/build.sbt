libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.4" % "test"

enablePlugins(JettyPlugin)

test := (Jetty / test).value
