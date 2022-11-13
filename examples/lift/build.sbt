libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "net.liftweb" %% "lift-webkit" % "3.4.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

enablePlugins(JettyPlugin)

test := (Jetty / test).value
