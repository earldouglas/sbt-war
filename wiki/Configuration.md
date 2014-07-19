Plugin keys are located in [`com.earldouglas.xsbtwebplugin.PluginKeys`](https://github.com/earldouglas/xsbt-web-plugin/blob/master/src/main/scala/PluginKeys.scala).

## Container settings

```scala
host in container.Configuration := "192.168.1.4"

port in container.Configuration := 8080

ssl in container.Configuration := Some("192.168.1.4", 8443, "keystore_path", "keystore_password", "key_password")

customConfiguration in container.Configuration := true

configurationFiles in container.Configuration := Seq(file("jetty.xml"))

configurationXml in container.Configuration := <xml />
```

## Web application settings

```scala
webappResources in Compile <+= (sourceDirectory in Runtime)(sd => sd / "static")

scanDirectories in Compile += file("lib")

scanInterval in Compile := 0

env in Compile := Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile)

fullClasspath in Runtime in packageWar <+= baseDirectory.map(bd => bd / "extras")

classesAsJar in Compile := true
```