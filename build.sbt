organization := "com.earldouglas"

version := "0.4.2"

scalaVersion := "2.10.2"

crossScalaVersions ++= Seq("2.9.0", "2.9.1", "2.9.2", "2.9.3")

sbtPlugin := true

scalacOptions += "-deprecation"

scriptedBufferLog := false

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }
