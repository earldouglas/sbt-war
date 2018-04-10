scalaVersion := "2.12.5"

libraryDependencies += "javax.servlet" %  "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "net.liftweb"   %% "lift-webkit"       % "3.2.0"


enablePlugins(JettyPlugin)
