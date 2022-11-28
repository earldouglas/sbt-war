scalaVersion := "2.11.12" // spray only goes up to 2.11

libraryDependencies += "io.spray" %% "spray-servlet" % "1.3.4"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.31"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

enablePlugins(JettyPlugin)
