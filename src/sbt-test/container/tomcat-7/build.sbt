name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(TomcatPlugin)

containerLibs in Tomcat := Seq("com.github.jsimone" % "webapp-runner" % "7.0.34.1" intransitive())

containerMain in Tomcat := "webapp.runner.launch.Main"
