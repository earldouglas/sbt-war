seq(ScriptedPlugin.scriptedSettings :_*)

sbtPlugin := true

organization := "com.github.siasia"

name := "xsbt-web-plugin"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-tools.sbt" %% "web-app" % "0.9.4-SNAPSHOT",
  "org.scala-tools.sbt" %% "classpath" % "0.9.4-SNAPSHOT"
)
