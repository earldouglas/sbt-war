name := "test"

scalaVersion := "2.10.2"

version := "0.1.0-SNAPSHOT"

enablePlugins(WebappPlugin)

sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "WebContent"
