# xsbt-web-plugin

*Current release: 3.0.0*

xsbt-web-plugin is an [sbt] extension for building [J2EE][j2ee] Web
applications in Scala and Java.  It is best suited for projects that:

* Deploy to common cloud platforms (e.g. [Google App Engine][gae],
  [Heroku][heroku], [Elastic Beanstalk][ebs], [Jelastic][jelastic])
* Deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish,
  WebSphere)
* Incorporate J2EE libraries (e.g. [JSP][jsp], [JSF][jsf], [EJB][ejb])
* Utilize J2EE technologies (e.g. [`Servlet`][servlet],
  [`Filter`][filter], [JNDI][jndi])
* Have a specific need to be packaged as a [*.war* file][war]

For previous releases, see the [docs](docs) directory.  Releases follow
[Specified Versioning][specver] guidelines.

## Requirements

* Scala 2.10.2+
* sbt 0.13.6+

*Scala 2.11 and 2.12 are [not yet supported by sbt][issues/166].*

## Quick reference

Add xsbt-web-plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "3.0.0")
```

Enable the Jetty plugin:

*build.sbt*:

```scala
enablePlugins(JettyPlugin)
```

From the sbt console:

* Start (or restart) the container with `jetty:start`
* Stop the container with `jetty:stop`
* Build a *.war* file with `package`

To use Tomcat instead of Jetty:

* Substitute `TomcatPlugin` for `JettyPlugin`
* Substitute `tomcat:start` for `jetty:start`
* Substitute `tomcat:stop` for `jetty:stop`

## Starting from scratch

Create a new empty project:

```
mkdir myproject
cd myproject
```

Set up the project structure:

```
mkdir project
mkdir -p src/main/scala
mkdir -p src/main/webapp/WEB-INF
```

Configure sbt:

*project/build.properties:*

```
sbt.version=0.13.8
```

*project/build.sbt:*

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "3.0.0")
```

*build.sbt:*

```scala
scalaVersion := "2.11.6"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)
```

Add a servlet:

*src/main/scala/servlets.scala*:

```scala
package servlets

import javax.servlet.http._

class MyServlet extends HttpServlet {

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")
    response.getWriter.write("""<h1>Hello, world!</h1>""")
  }

}
```

*src/main/webapp/WEB-INF/web.xml*:

```xml
<web-app>

  <servlet>
    <servlet-name>my servlet</servlet-name>
    <servlet-class>servlets.MyServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>my servlet</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>

</web-app>
```

## Configuration and use

### Triggered execution

xsbt-web-plugin supports sbt's [triggered execution][3] by prefixing
commands with `~`.

*sbt console:*

```
> ~jetty:start
```

This starts the Jetty container, then monitors the sources, resources,
and webapp directories for changes, which triggers a container restart.

### Container arguments

To pass extra arguments to the Jetty or Tomcat container, set
`containerArgs`:

```scala
containerArgs := Seq("--path", "/myservice")
```

* For available Jetty arguments, see the [Jetty Runner docs][4]
* For available Tomcat arguments, see [webapp-runner#options][5]

### Custom container

To use a custom J2EE container, e.g. a main class named `runner.Run`,
enable `ContainerPlugin` and set `containerLibs` and
`containerLaunchCmd`:

```scala
enablePlugins(ContainerPlugin)

containerLibs in Container := Seq(
    "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115"
  , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115"
  , "test"              %% "runner"       % "0.1.0-SNAPSHOT"
)

containerLaunchCmd in Container :=
  { (port, path) => Seq("runner.Run", port.toString, path) }
```

*sbt:*

```
> container:start
> container:stop
```

*Example: [container/custom-runner][6]*

### Forked JVM options

To set system properties for the forked container JVM, set
`containerForkOptions`:

```scala
containerForkOptions := new ForkOptions(runJVMOptions = Seq("-Dh2g2=42"))
```

*Example: [container/fork-options][7]*

Alternatively, set `javaOptions` in the `Jetty` (or `Tomcat`)
configuration:

```scala
javaOptions in Jetty += "-Dh2g2=42"
```

To attach an Eclipse debugger, set `-Xdebug` and `-Xrunjdwp`:

*build.sbt:*

```scala
javaOptions in Jetty ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
)
```

This is a handy way to change configuration for local development and
testing:

```scala
javaOptions in Jetty += "-DdbUrl=jdbc:sqlite:test.db"
```

In Eclipse, create and run a new *Remote Java Application* launch
configuration with a *Connection Type* of *Scala debugger (Socket
Attach)*, set to connect to *localhost* on port *8000*.

*Example: [container/java-options][8]*

Similarly, to attach an IntelliJ IDEA debugger, add a Remote run configuration:
*Run* -> *Edit Configurations...*
Under *Defaults* select *Remote* and push the "+" button to add a new configuration.
By default the configuration will use port 5005.  (Use the same port in the -Xrunjdwp address.)
Name this configuration, and run it in debug mode.

### Debug mode

To enable debugging through [JDWP][jdwp], use `jetty:debug` or
`tomcat:debug`.  Optionally set `debugAddress`, which defaults to
`"debug"` under Windows and `"8888"` otherwise, and `debugOptions`,
which defaults to:

```scala
port =>
  Seq( "-Xdebug"
     , Seq( "-Xrunjdwp:transport=dt_socket"
          , "address=" + port
          , "server=y"
          , "suspend=n"
          ).mkString(",")
     )
```

### Jetty version

By default, Jetty 9.4.1 is used.  To use a different version, set
`containerLibs`:

```scala
containerLibs in Jetty := Seq("org.mortbay.jetty" % "jetty-runner" % "7.0.0.v20091005" intransitive())
```

Depending on the version, it may also be necessary to specify the name
of Jetty's runner:

```scala
containerMain := "org.mortbay.jetty.runner.Runner"
```

*Examples:*

* *[container/jetty-7][9]*
* *[container/jetty-8][10]*
* *[container/jetty-9][11]*

### Container port

By default, the container runs on port *8080*.  To use a different port,
set `containerPort`:

```scala
containerPort := 9090
```

*Examples:*

* *[container/jetty-port-9090][12]*
* *[container/tomcat-port-9090][13]*

### *jetty.xml*

To use a *jetty.xml* configuration file, set `containerConfigFile`:

```scala
containerConfigFile := Some(file("etc/jetty.xml"))
```

This option can be used to enable SSL and HTTPS.

*Examples:*

* *[container/jetty-xml-http][14]*
* *[container/jetty-xml-https][15]*

### Multi-project applications

*Examples:*

* *[container/multi-module-single-webapp][16]*
* *[container/multi-module-multi-webapp][17]*

### Tomcat version

By default, Tomcat 8.5.9.0 is used.  To use a different version, set
`containerLibs`:

```scala
containerLibs in Tomcat := Seq("com.github.jsimone" % "webapp-runner" % "7.0.34.1" intransitive())
```

Depending on the version, it may also be necessary to specify the name
of Tomcat's runner:

```scala
containerMain in Tomcat := "webapp.runner.launch.Main"
```

### Renaming the *.war* file

This can be useful for keeping the version number out of the *.war* file
name, using a non-conventional file name or path, adding additional
information to the file name, etc.

```scala
artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}
```
See ["Modifying default artifacts"][artifacts] in the sbt documentation
for additional information.

### Massaging the *.war* file

After the *<project>/target/webapp* directory is prepared, it can be
modified with an arbitrary `File => Unit` function by setting
`webappPostProcess`.

To list the contents of the *webapp* directory after it is prepared:

```scala
webappPostProcess := {
  webappDir: File =>
    def listFiles(level: Int)(f: File): Unit = {
      val indent = ((1 until level) map { _ => "  " }).mkString
      if (f.isDirectory) {
        streams.value.log.info(indent + f.getName + "/")
        f.listFiles foreach { listFiles(level + 1) }
      } else streams.value.log.info(indent + f.getName)
    }
    listFiles(1)(webappDir)
}
```

To include webapp resources from multiple directories in the prepared
*webapp* directory:

```scala
webappPostProcess := {
  webappDir: File =>
    val baseDir = baseDirectory.value / "src" / "main"
    IO.copyDirectory(baseDir / "webapp1", webappDir)
    IO.copyDirectory(baseDir / "webapp2", webappDir)
    IO.copyDirectory(baseDir / "webapp3", webappDir)
}
```

*Examples:*

* *[war/simple][18]*
* *[webapp/yuicompressor][19]*

### Custom resources directory

Files in the extra resource directory are not compiled, and are bundled
directly in the project artifact *.jar* file.

To add a custom resources directory, set `unmanagedResourceDirectories`:

```scala
unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

*Example: [webapp/unmanaged-resources][20]*

### Custom sources directory

Scala files in the extra source directory are compiled, and bundled in
the project artifact *.jar* file.

To add a custom sources directory, set `unmanagedSourceDirectories`:

```scala
unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

*Example: [webapp/unmanaged-sources][21]*

### Utilizing *WEB-INF/classes*

By default, project classes are packaged into a *.jar* file, shipped in
the *WEB-INF/lib* directory of the *.war* file.  To instead keep them
extracted in *WEB-INF/classes*, set `webappWebInfClasses`:

```scala
webappWebInfClasses := true
```

*Examples:*

* *[webapp/web-inf-classes][22]*
* *[webapp/web-inf-lib][23]*

### Web application destination

The Web application destination directory is where the static Web
content, compiled Scala classes, library *.jar* files, etc. are placed.
By default, they go to *<project>/target/webapp*.

To specify a different directory, set `target` in the `webappPrepare`
configuration:

```scala
target in webappPrepare := target.value / "WebContent"
```

*Example: [webapp/webapp-dest][24]*

### Web application resources

The Web application resources directory is where static Web content
(including *.html*, *.css*, and *.js* files, the *web.xml* container
configuration file, etc.  By default, this is kept in
*<project>/src/main/webapp*.

To specify a different directory, set `sourceDirectory` in the
`webappPrepare` configuration:

```scala
sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "WebContent"
```

*Example: [webapp/webapp-src][25]*

### Prepare the Web application for execution and deployment

For situations when the prepared *<project>/target/webapp* directory is
needed, but the packaged *.war* file isn't.

*sbt console:*

```
webappPrepare
```

### Add manifest attributes

Manifest attributes of the *.war* file can be configured via
`packageOptions in sbt.Keys.package` in *build.sbt*:

```scala
packageOptions in sbt.Keys.`package` +=
  Package.ManifestAttributes( java.util.jar.Attributes.Name.SEALED -> "true" )
```

### Inherit manifest attributes

To configure the *.war* file to inherit the manifest attributes of the
*.jar* file, typically set via `packageOptions in (Compile,
packageBin)`, set `inheritJarManifest` to `true`:

```scala
inheritJarManifest := true
```

### Container shutdown and sbt

By default, sbt will shutdown the running container when exiting sbt.

To allow the container to continue running after sbt exits, set
`containerShutdownOnExit`:

```scala
containerShutdownOnExit := false
```

## Deploying to Heroku

See [sbt-heroku-deploy][26].

## Deploying to Elastic Beanstalk

Before trying to deploy anything, create an application and a
Tomcat-based environment for it in Elastic Beanstalk.

Enable the `ElasticBeanstalkDeployPlugin` plugin, and configure your
application's name, environment, and region:

```scala
enablePlugins(ElasticBeanstalkDeployPlugin)
elasticBeanstalkAppName := "xwp-getting-started"
elasticBeanstalkEnvName := "xwp-getting-started"
elasticBeanstalkRegion  := "us-west-1"
```

Add AWS credentials to your environment, launch sbt, and deploy your
application by running `elasticBeanstalkDeploy`:

```
$ AWS_ACCESS_KEY=foo AWS_SECRET_KEY=bar sbt
> elasticBeanstalkDeploy
```

## Block sbt on running container

To start the container from the command line and block sbt from exiting
prematurely, use `jetty:join`:

```
$ sbt jetty:start jetty:join
```

This is useful for running sbt in production (e.g. in a Docker
container).

### Quickstart mode

The development cycle can be sped up by serving static resources
directly from source, and avoiding packaging of compiled artifacts.

Use `<container>:quickstart` in place of `<container>:start` to run the
container in quickstart mode:

```
> jetty:quickstart
```

Note that this necessarily circumvents any behavior set in
`webappPostProcess`.

### Running multiple containers

To launch using more than a single container, set `containerScale`:

```scala
containerScale := 5
```

This will configure the container to launch in five forked JVMs, using
five sequential ports starting from `containerPort`.

In debug mode, five additional sequential debug ports starting from
`debugPort` will be opened.

#### JRebel integration

The development cycle can be further sped up by skipping server restarts
between code recompilation.

Add `-agentpath` to the container's JVM options:

```
javaOptions in Jetty += "-agentpath:/path/to/jrebel/lib/libjrebel64.so"
```

Launch the container with `quickstart`, and run triggered compilation:

```
> jetty:quickstart
> ~compile
```

[3]: http://www.scala-sbt.org/0.13/docs/Triggered-Execution.html
[4]: http://www.eclipse.org/jetty/documentation/current/runner.html#_full_configuration_reference
[5]: https://github.com/jsimone/webapp-runner#options
[6]: src/sbt-test/container/custom-runner
[7]: src/sbt-test/container/fork-options
[8]: src/sbt-test/container/java-options
[9]: src/sbt-test/container/jetty-7
[10]: src/sbt-test/container/jetty-8
[11]: src/sbt-test/container/jetty-9
[12]: src/sbt-test/container/jetty-port-9090
[13]: src/sbt-test/container/tomcat-port-9090
[14]: src/sbt-test/container/jetty-xml-http
[15]: src/sbt-test/container/jetty-xml-https
[16]: src/sbt-test/container/multi-module-single-webapp
[17]: src/sbt-test/container/multi-module-multi-webapp
[18]: src/sbt-test/war/simple
[19]: src/sbt-test/webapp/yuicompressor
[20]: src/sbt-test/webapp/unmanaged-resources
[21]: src/sbt-test/webapp/unmanaged-sources
[22]: src/sbt-test/webapp/web-inf-classes
[23]: src/sbt-test/webapp/web-inf-lib
[24]: src/sbt-test/webapp/webapp-dest
[25]: src/sbt-test/webapp/webapp-src
[26]: https://github.com/earldouglas/sbt-heroku-deploy
[artifacts]: http://www.scala-sbt.org/0.13/docs/Artifacts.html#Modifying+default+artifacts
[ebs]: https://console.aws.amazon.com/elasticbeanstalk/home
[ejb]: http://en.wikipedia.org/wiki/Ejb
[filter]: http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html
[gae]: https://developers.google.com/appengine/
[heroku]: https://www.heroku.com/
[issues/166]: https://github.com/earldouglas/xsbt-web-plugin/issues/166
[j2ee]: http://en.wikipedia.org/wiki/Java_Servlet
[jdwp]: https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/introclientissues005.html
[jelastic]: http://jelastic.com/
[jndi]: http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface
[jsf]: http://en.wikipedia.org/wiki/JavaServer_Faces
[jsp]: http://en.wikipedia.org/wiki/JavaServer_Pages
[sbt]: http://www.scala-sbt.org/
[servlet]: http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html
[specver]: https://earldouglas.com/posts/specver.html
[war]: https://en.wikipedia.org/wiki/WAR_%28Sun_file_format%29
