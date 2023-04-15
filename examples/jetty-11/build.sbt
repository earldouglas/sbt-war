libraryDependencies += "jakarta.servlet" % "jakarta.servlet-api" % "5.0.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"

enablePlugins(JettyPlugin)

containerLibs in Jetty := Seq(
  "org.eclipse.jetty" % "jetty-runner" % "11.0.14" intransitive ()
)
