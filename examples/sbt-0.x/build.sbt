scalaVersion := "2.10.2"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

enablePlugins(JettyPlugin)
