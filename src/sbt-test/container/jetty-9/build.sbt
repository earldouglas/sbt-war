name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.3.13.v20161014" intransitive())

containerMain in Jetty := "org.eclipse.jetty.runner.Runner"
