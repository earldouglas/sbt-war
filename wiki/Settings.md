Container settings:
```scala
port in container.Configuration := 8081

ssl in container.Configuration := Some(ssl_port, "path_to_keystore", "keystore_password", "key_password")

customConfiguration in container.Configuration := true

configurationFiles in container.Configuration := Seq(file("jetty.xml"))

configurationXml in container.Configuration := <xml />
```

Webapp settings:
```scala
webappResources in Compile <+= (sourceDirectory in Runtime)(sd => sd / "static")

scanDirectories in Compile += file("lib")

scanInterval in Compile := 0

env in Compile := Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile)
```