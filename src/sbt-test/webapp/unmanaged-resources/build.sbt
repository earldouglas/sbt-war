name := "test"

version := "0.1.0-SNAPSHOT"

unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")

webappSettings
