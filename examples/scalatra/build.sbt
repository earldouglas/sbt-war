libraryDependencies += "org.scalatra" %% "scalatra" % "2.7.1"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

enablePlugins(JettyPlugin)

Test / fork := true
