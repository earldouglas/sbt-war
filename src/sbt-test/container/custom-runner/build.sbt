name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

lazy val root = (project in file("."))
  .enablePlugins(ContainerPlugin)
  .settings(
    containerLibs in Container := Seq(
      "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115"
    , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115"
    , "test"              %% "runner"       % "0.1.0-SNAPSHOT"
    )
  , containerLaunchCmd in Container := Seq("runner.Run", "8080", (target in webappPrepare).value.absolutePath)
  )

lazy val runner = project
