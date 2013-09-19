[![Build Status](http://jenkins.jamestastic.com/job/xsbt-web-plugin/badge/icon)](http://jenkins.jamestastic.com/job/xsbt-web-plugin/)

## Quick start

Add plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")
```

For *.sbt* build definitions, inject the plugin settings in *build.sbt*:

```scala
seq(webSettings :_*)
```

For *.scala* build definitions, inject the plugin settings in *Build.scala*:

```scala
Project(..., settings = Project.defaultSettings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
```

Include Jetty in the *container* classpath:

```scala
libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
```

## Examples

For a basic project template and walkthrough, see the [xwp-template](https://github.com/JamesEarlDouglas/xwp-template) project.

There are also several examples in the *[sbt-test](https://github.com/JamesEarlDouglas/xsbt-web-plugin/tree/master/src/sbt-test/web)* directory.

## Configuration

Plugin keys are located in `com.earldouglas.xsbtwebplugin.PluginKeys`

### Container settings

```scala
port in container.Configuration := 8081

ssl in container.Configuration := Some(ssl_port, "keystore_path", "keystore_password", "key_password")

customConfiguration in container.Configuration := true

configurationFiles in container.Configuration := Seq(file("jetty.xml"))

configurationXml in container.Configuration := <xml />
```

### Web application settings

```scala
webappResources in Compile <+= (sourceDirectory in Runtime)(sd => sd / "static")

scanDirectories in Compile += file("lib")

scanInterval in Compile := 0

env in Compile := Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile)

fullClasspath in Runtime in packageWar <+= baseDirectory.map(bd => bd / "extras")

classesAsJar in Compile := true
```

## Content

Web application content belongs in the `src/main/webapp` directory.  For example, `web.xml` should be placed in `src/main/webapp/WEB-INF/web.xml`.

## Commands

To start a web application, use `container:start`:

```
> container:start
[info] jetty-7.3.0.v20110203
[info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
[info] started o.e.j.w.WebAppContext{/,[file:/home/siasia/projects/xsbt-web-plugin/src/main/webapp/]}
[info] Started SelectChannelConnector@0.0.0.0:8080
```

The application is now accesible on localhost, port 8080: [http://localhost:8080](http://localhost:8080)

To stop a running web application, use `container:stop`:

```
> container:stop
[info] stopped o.e.j.w.WebAppContext{/,[file:/home/siasia/projects/xsbt-web-plugin/src/main/webapp/]}
```
    
To reload a running web application, use `container:reload <context-path>`:

```
> container:reload /
```

To automatically reload a web application when source code is changed, use `~;container:start; container:reload <context-path>`:

```
> ~;container:start; container:reload /
```

To build a WAR package, use `package`.  To change the output directory of the WAR file, modify its `artifactPath`:

```scala
artifactPath in (Compile, packageWar) ~= { defaultPath =>
  file("dist") / defaultPath.getName
}
```

## Artifacts

### *.war* file

To disable publishing of the *.war* file, add the setting:

```scala
packagedArtifacts <<= packagedArtifacts map { as => as.filter(_._1.`type` != "war") }
```

Note that `package` can still be used to create the *.war* file under the project *target/* directory.

### *.jar* file

To enable publishing of the project's *.jar* file, add the setting:

```scala
publishArtifact in (Compile, packageBin) := true
```

## More information

See the [wiki](http://github.com/JamesEarlDouglas/xsbt-web-plugin/wiki/)

## License

This software is distributed under modified 3-clause BSD license. See [LICENSE](https://github.com/JamesEarlDouglas/xsbt-web-plugin/blob/master/LICENSE) for more information.
