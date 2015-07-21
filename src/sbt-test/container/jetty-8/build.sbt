name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerLibs in Jetty := Seq("org.mortbay.jetty" % "jetty-runner" % "8.0.0.v20110901" intransitive())

containerMain in Jetty := "org.mortbay.jetty.runner.Runner"
