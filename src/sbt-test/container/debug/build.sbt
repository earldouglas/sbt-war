name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" intransitive())

containerMain in Jetty := "org.eclipse.jetty.runner.Runner"
