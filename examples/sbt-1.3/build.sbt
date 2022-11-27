scalaVersion := "3.2.1"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

enablePlugins(JettyPlugin)

test := (Jetty / test).value
