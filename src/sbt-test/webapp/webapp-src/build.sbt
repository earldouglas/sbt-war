name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

webappSrc in webapp <<= (sourceDirectory in Compile) map  { _ / "WebContent" }
