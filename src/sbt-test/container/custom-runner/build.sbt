libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(ContainerPlugin)

containerLibs in Container += "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115"
containerLibs in Container += "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115"
containerLibs in Container += "runner" %% "runner" % "0.1.0-SNAPSHOT"

containerLaunchCmd in Container := { (port, path) =>
  Seq("runner.Run", port.toString, path)
}

lazy val root = (project in file("."))

lazy val runner = project
