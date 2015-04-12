name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

target in webappPrepare := target.value / "WebContent"
