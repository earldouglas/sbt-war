seq(ScriptedPlugin.scriptedSettings :_*)

sbtPlugin := true

ScriptedPlugin.scriptedBufferLog := false

organization := "com.github.siasia"

name := "xsbt-web-plugin"

version := "0.1-SNAPSHOT"

libraryDependencies <<= (libraryDependencies, appConfiguration) {
  (deps, app) =>
  val version = app.provider.id.version
  deps ++ Seq(
    "org.scala-tools.sbt" %% "classpath" % version
  )
}
