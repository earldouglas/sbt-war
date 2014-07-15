name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

webappSettings

runnerContainer(
    port   = 8080
  , runner = Some("org.mortbay.jetty" % "jetty-runner" % "8.0.0.v20110901" %
                  "container" intransitive())
  , main   = "org.mortbay.jetty.runner.Runner"
)
