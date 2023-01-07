libraryDependencies += "org.scalatra" %% "scalatra" % "2.8.4"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"

enablePlugins(JettyPlugin)

Test / fork := true
