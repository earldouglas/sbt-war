scalaVersion := "2.12.11"

libraryDependencies += "org.scalatra" %% "scalatra" % "2.5.0"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.scalatra" %% "scalatra-scalatest" % "2.5.0" % "test"

enablePlugins(JettyPlugin)

fork in Test := true
