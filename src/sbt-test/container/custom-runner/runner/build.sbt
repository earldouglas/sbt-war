organization := "test"

name := "runner"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "provided"
  , "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "provided"
  )
