enablePlugins(JettyPlugin)

javacOptions ++= Seq("-encoding", "UTF-8")

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

test := (Jetty / test).value
