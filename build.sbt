// general
organization  := "com.earldouglas"

name          := "xsbt-web-plugin"

scalaVersion  := "2.10.2"

sbtPlugin     := true

scalacOptions ++= Seq("-feature", "-deprecation")

// bintray-sbt
publishMavenStyle := false

licenses          += ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))

// scripted-plugin
ScriptedPlugin.scriptedSettings

scriptedBufferLog  := false

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

watchSources       <++= sourceDirectory map { path => (path ** "*").get }
