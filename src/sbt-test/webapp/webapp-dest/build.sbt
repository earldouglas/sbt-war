name := "test"

version := "0.1.0-SNAPSHOT"

webappDest in webapp <<= target map  { _ / "WebContent" }

webappSettings
