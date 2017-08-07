name := "test"

scalaVersion := "2.10.6"

version := "0.1.0-SNAPSHOT"

unmanagedResourceDirectories in Compile += (sourceDirectory in Compile).value / "extra"

enablePlugins(WebappPlugin)
