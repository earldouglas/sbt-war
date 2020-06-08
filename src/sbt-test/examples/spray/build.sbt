scalaVersion := "2.10.7"

libraryDependencies += "io.spray" % "spray-servlet" % "1.3.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.16"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

enablePlugins(JettyPlugin)
