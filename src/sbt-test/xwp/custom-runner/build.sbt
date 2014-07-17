name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

runnerContainer(
  Seq(
      "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115" % "container"
    , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115" % "container"
    , "test"              %% "runner"       % "0.1.0-SNAPSHOT"  % "container"
  ),
  Seq("runner.Run", "8080")
) ++ warSettings ++ webappSettings

lazy val root = (project in file("."))

lazy val runner = project
