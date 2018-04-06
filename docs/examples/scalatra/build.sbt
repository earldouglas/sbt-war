scalaVersion := "2.12.5"

libraryDependencies += "javax.servlet" %  "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "org.scalatra"  %% "scalatra"          % "2.5.0"

enablePlugins(JettyPlugin)
