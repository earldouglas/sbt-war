scalaVersion := "2.10.2"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

enablePlugins(TomcatPlugin)

fork in run := true
connectInput in run := true

cancelable in Global := true
