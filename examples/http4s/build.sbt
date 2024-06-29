val http4sVersion = "1.0.0-M38"

ThisBuild / scalaVersion := "3.2.2"

libraryDependencies += "org.http4s" %% "http4s-dsl" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-servlet" % http4sVersion
libraryDependencies += "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

Jetty / containerLibs := Seq(
  "org.eclipse.jetty" % "jetty-runner" % "11.0.15" intransitive ()
)

enablePlugins(JettyPlugin)
