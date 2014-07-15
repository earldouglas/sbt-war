name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

webappSettings

runnerContainer(
    port   = 8080
  , runner = Some("com.github.jsimone" % "webapp-runner" % "7.0.34.1" %
                  "container" intransitive())
  , main   = "webapp.runner.launch.Main"
)
