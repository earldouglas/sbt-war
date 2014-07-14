name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
