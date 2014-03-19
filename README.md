[![Build Status](https://travis-ci.org/earldouglas/xsbt-web-plugin.png?branch=master)](https://travis-ci.org/earldouglas/xsbt-web-plugin)

xsbt-web-plugin is an extension to [sbt](http://www.scala-sbt.org/) for building enterprise Web applications based on the [Java J2EE Servlet specification](http://en.wikipedia.org/wiki/Java_Servlet).

xsbt-web-plugin supports both Scala and Java, and is best suited for projects that:

* deploy to common cloud platforms (e.g. [Google App Engine](https://developers.google.com/appengine/), [Heroku](https://www.heroku.com/), [Elastic Beanstalk](https://console.aws.amazon.com/elasticbeanstalk/home), [Jelastic](http://jelastic.com/))
* deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish, WebSphere)
* incorporate J2EE libraries (e.g. [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages), [JSF](http://en.wikipedia.org/wiki/JavaServer_Faces), [EJB](http://en.wikipedia.org/wiki/Ejb))
* utilize J2EE technologies (e.g. [`Servlet`](http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html)s, [`Filter`](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html)s, [JNDI](http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface))
* have a specific need to be packaged as a *.war* file

## Quick start

The quickest way to get started is to clone the [xwp-template](https://github.com/JamesEarlDouglas/xwp-template) project, which sets up the necessary directories, files, and configuration for a basic xsbt-web-plugin project.

## Basics

First, add xsbt-web-plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")
```

For [*.sbt* build definitions](http://www.scala-sbt.org/release/docs/Getting-Started/Basic-Def.html), inject the plugin settings in *build.sbt*:

```scala
seq(webSettings :_*)
```

For [*.scala* build definitions](http://www.scala-sbt.org/release/docs/Getting-Started/Full-Def.html), inject the plugin settings in *Build.scala*:

```scala
Project(..., settings = Project.defaultSettings ++
                          com.earldouglas.xsbtwebplugin.WebPlugin.webSettings)
```

Include Jetty on the *container* classpath:

*Jetty:*
```scala
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container"
)
```

If you prefer Tomcat to Jetty, include it on the *container* classpath instead:

*Tomcat:*
```scala
libraryDependencies ++= Seq(
  "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % "container"
)
```

## Configuration

Plugin keys are located in [`com.earldouglas.xsbtwebplugin.PluginKeys`](https://github.com/earldouglas/xsbt-web-plugin/blob/master/src/main/scala/PluginKeys.scala).

### Container settings

```scala
host in container.Configuration := "192.168.1.4"

port in container.Configuration := 8080

ssl in container.Configuration := Some("192.168.1.4", 8443, "keystore_path", "keystore_password", "key_password")

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
