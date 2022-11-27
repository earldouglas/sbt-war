libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.21.33"
libraryDependencies += "org.http4s" %% "http4s-servlet" % "0.21.33"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % "test"

enablePlugins(JettyPlugin)

test := (Jetty / test).value
