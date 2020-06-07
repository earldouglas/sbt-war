scalaVersion := "2.12.11"

enablePlugins(JettyPlugin)

javacOptions ++= Seq("-encoding", "UTF-8")

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
