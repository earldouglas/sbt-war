name := "test"

scalaVersion := "2.10.6"

version := "0.1.0-SNAPSHOT"

enablePlugins(WebappPlugin)

target in webappPrepare := target.value / "WebContent"
