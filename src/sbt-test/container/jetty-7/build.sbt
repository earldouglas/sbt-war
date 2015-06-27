name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerLibs := Seq("org.mortbay.jetty" % "jetty-runner" % "7.0.0.v20091005" intransitive())

containerMain := "org.mortbay.jetty.runner.Runner"
