[![Build Status](https://travis-ci.org/earldouglas/xsbt-web-plugin.png?branch=master)](https://travis-ci.org/earldouglas/xsbt-web-plugin)

## News

**July 13, 2014**

Preview the upcoming changes in version 1.0.0 - see the [1.0 branch](https://github.com/earldouglas/xsbt-web-plugin/tree/1.0#quick-reference).

## About

xsbt-web-plugin is an extension to [sbt](http://www.scala-sbt.org/) for building enterprise Web applications based on the [Java J2EE Servlet specification](http://en.wikipedia.org/wiki/Java_Servlet).

xsbt-web-plugin supports both Scala and Java, and is best suited for projects that:

* Deploy to common cloud platforms (e.g. [Google App Engine](https://developers.google.com/appengine/), [Heroku](https://www.heroku.com/), [Elastic Beanstalk](https://console.aws.amazon.com/elasticbeanstalk/home), [Jelastic](http://jelastic.com/))
* Deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish, WebSphere)
* Incorporate J2EE libraries (e.g. [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages), [JSF](http://en.wikipedia.org/wiki/JavaServer_Faces), [EJB](http://en.wikipedia.org/wiki/Ejb))
* Utilize J2EE technologies (e.g. [`Servlet`](http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html)s, [`Filter`](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html)s, [JNDI](http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface))
* Have a specific need to be packaged as a [*.war* file](https://en.wikipedia.org/wiki/WAR_%28Sun_file_format%29)

## Requirements

* sbt 0.13.x
* Scala 2.10.x

## Getting started 

The quickest way to get started is to clone the [xwp-template](https://github.com/earldouglas/xwp-template) 
project, which sets up the necessary directories, files, and configuration for a 
basic xsbt-web-plugin project.

There are many examples in the form of tests in [src/sbt-test](https://github.com/earldouglas/xsbt-web-plugin/tree/master/src/sbt-test).

For more information, please see the [quick reference](#quick-reference) or the 
[wiki](http://github.com/earldouglas/xsbt-web-plugin/wiki/).

## How it works

xsbt-web-plugin consists of three modules: a *webapp* plugin, a *war* plugin, 
and a *container* plugin.

The *webapp* plugin is responsible for preparing a Servlet-based Web application 
as a directory, containing compiled project code, project resources, and a 
special *webapp* directory (which includes the *web.xml* configuration file, 
static HTML files, etc.).

The *war* plugin builds on the *webapp* plugin, adding a way to package the Web 
application directory as a *.war* file that can be published as an artifact, and 
deployed to a Servlet container.

The *container* plugin also builds on the *webapp* plugin, adding a way to 
launch a servlet container in a forked JVM to host the project as a Web 
application.

Put together, these compose xsb-web-plugin, and provide complete support for 
developing Servlet-based Web applications in Scala (and Java).

## Quick reference

First, add xsbt-web-plugin:

*project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.0.0-M1")
```

Then choose either Jetty or Tomcat with default setings:

*build.sbt*:

```scala
jetty()
```

*build.sbt*:

```scala
tomcat()
```

Start (or restart) the container with `container:start`:

*sbt console:*

```
> container:start
```

Stop the container with `container:stop`:

*sbt console:*

```
> container:stop
```

Build a *.war* file with `package`:

*sbt console:*

```
> package
```

## Configuration and usage

**Triggered (re)launch**

*sbt console:*

```
> ~container:start
```

This starts the container, then monitors the sources, resources, and webapp 
directories for changes, which triggers a container restart.

**Configure Jetty to run on port 9090**

*build.sbt:*

```scala
jetty(port = 9090)
```

**Configure Tomcat to run on port 9090**

*build.sbt:*

```scala
tomcat(port = 9090)
```

**Configure Jetty with jetty.xml**

*build.sbt:*

```scala
jetty(config = "etc/jetty.xml")
```

The `config` path can be either absolute or relative to the project directory.

**Depend on libraries in a multi-project build**

*build.sbt:*

```scala
lazy val root = (project in file(".")) aggregate(mylib1, mylib2, mywebapp)

lazy val mylib1 = project

lazy val mylib2 = project

lazy val mywebapp = project webappDependsOn (mylib1, mylib2)
```

Here we use `webappDependsOn` in place of the usual `dependsOn` function (which 
will be called automatically).

**Add an additional source directory**

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional source directory
unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

Scala files in the extra source directory are compiled, and bundled in the 
project artifact *.jar* file.

**Add an additional resource directory**

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional resource directory
unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

Files in the extra resource directory are not compiled, and are bundled directly 
in the project artifact *.jar* file.

**Change the default Web application resources directory**

*build.sbt:*

```scala
// set <project>/src/main/WebContent as the webapp resources directory
webappSrc in webapp <<= (sourceDirectory in Compile) map  { _ / "WebContent" }
```

The Web application resources directory is where static Web content (including 
*.html*, *.css*, and *.js* files, the *web.xml* container configuration file, 
etc.  By default, this is kept in *<project>/src/main/webapp*.

**Change the default Web application destination directory**

*build.sbt:*

```scala
// set <project>/target/WebContent as the webapp destination directory
webappDest in webapp <<= target map  { _ / "WebContent" }
```

The Web application destination directory is where the static Web content, 
compiled Scala classes, library *.jar* files, etc. are placed.  By default, 
they go to *<project>/target/webapp*.

**Modify the contents of the prepared Web application**

*project/plugins.sbt*:

```scala
libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7" intransitive()
```

After the *<project>/target/webapp* directory is prepared, it can be modified 
with an arbitrary `File => Unit` function.

*build.sbt:*

```scala
// minify the JavaScript file script.js to script-min.js
postProcess in webapp := {
  webappDir =>
    import java.io.File
    import com.yahoo.platform.yui.compressor.YUICompressor
    val src  = new File(webappDir, "script.js")
    val dest = new File(webappDir, "script-min.js")
    YUICompressor.main(Array(src.getPath, "-o", dest.getPath))
}
```

**Use *WEB-INF/classes* instead of *WEB-INF/lib***

By default, project classes and resources are packaged in the default *.jar* 
file artifact, which is copied to *WEB-INF/lib*.  This file can optionally be 
ignored, and the project classes and resources copied directly to 
*WEB-INF/classes*.

*build.sbt:*

```scala
webInfClasses in webapp := true
```

**Prepare the Web application for execution and deployment**

For situations when the prepared *<project>/target/webapp* directory is needed, 
but the packaged *.war* file isn't.

*sbt console:*

```
webapp:prepare
```

**Use a cusom webapp runner**

By default, either Jetty's [jetty-runner](http://wiki.eclipse.org/Jetty/Howto/Using_Jetty_Runner) 
or Tomcat's [webapp-runner](https://github.com/jsimone/webapp-runner) will be 
used to launch the container under `container:start`.

To use a custom runner, use `runnerContainer` with `warSettings` and 
`webappSettings`:

*build.sbt:*

```scala
runnerContainer(
  libs = Seq(
      "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115" % "container"
    , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115" % "container"
    , "test"              %% "runner"       % "0.1.0-SNAPSHOT"  % "container"
  ),
  args = Seq("runner.Run", "8080")
) ++ warSettings ++ webappSettings
```

Here, `libs` includes the `ModuleID`s of libraries needed to make our runner, 
which is invoked by calling the main method of `runner.Run` with a single 
argument to specify the server port.
