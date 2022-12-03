val http4sVersion = "0.21.33"

libraryDependencies += "org.http4s" %% "http4s-dsl" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-servlet" % http4sVersion
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

enablePlugins(JettyPlugin)
