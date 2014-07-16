name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

webappSettings

runnerContainer(
    libs = Seq("org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % 
               "container" intransitive())
  , args = Seq("org.eclipse.jetty.runner.Runner")
)
