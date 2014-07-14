name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

containerSettings

libraryDependencies += "com.github.jsimone" % "webapp-runner" % "7.0.34.1" % "container" intransitive()

launcher in container <<= tomcat in container
