name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "WebContent"
