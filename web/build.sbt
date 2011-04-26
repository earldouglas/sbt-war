organization := "com.github.siasia.sbt"

name := "web-app"

version := "0.1-SNAPSHOT"

libraryDependencies <<= (libraryDependencies, appConfiguration) {
  (deps, app) =>
  val version = app.provider.id.version
  deps ++ Seq(
    "org.scala-tools.sbt" %% "io" % version,
    "org.scala-tools.sbt" %% "logging" % version,
    "org.scala-tools.sbt" %% "classpath" % version,
    "org.scala-tools.sbt" %% "process" % version,
    "org.mortbay.jetty" % "jetty" % "6.1.14" % "optional",
    "org.mortbay.jetty" % "jetty-plus" % "6.1.14" % "optional",
    "org.eclipse.jetty" % "jetty-server" % "7.0.1.v20091125" % "optional",
    "org.eclipse.jetty" % "jetty-webapp" % "7.0.1.v20091125" % "optional",
    "org.eclipse.jetty" % "jetty-plus" % "7.0.1.v20091125" % "optional"
  )
}
