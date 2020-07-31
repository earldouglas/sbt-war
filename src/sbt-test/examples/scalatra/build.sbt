scalaVersion := "2.13.3"

libraryDependencies += "org.scalatra" %% "scalatra" % "2.7.0"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.scalatra" %% "scalatra-scalatest" % "2.7.0" % "test"

enablePlugins(JettyPlugin)

fork in Test := true
