name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

webappDest in webapp <<= target map  { _ / "WebContent" }
