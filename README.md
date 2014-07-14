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

The quickest way to get started is to clone the [xwp-template](https://github.com/JamesEarlDouglas/xwp-template) project, which sets up the necessary directories, files, and configuration for a basic xsbt-web-plugin project.

For more information, please see the [wiki](http://github.com/earldouglas/xsbt-web-plugin/wiki/).

## Quick reference

First, add xsbt-web-plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.10.0")
```

Inject the plugin settings in *build.sbt*:

```scala
xwpSettings
```

Specify either Jetty or Tomcat in the *container* configuration:

*Jetty:*

```scala
libraryDependencies += "org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % "container" intransitive()

launcher in container <<= jetty in container
```

*Tomcat:*

```scala
libraryDependencies += "com.github.jsimone" % "webapp-runner" % "7.0.34.1" % "container" intransitive()

launcher in container <<= tomcat in container
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

**Launch with Jetty**

*build.sbt:*

```scala
libraryDependencies += "org.eclipse.jetty" % "jetty-runner" % "9.2.1.v20140609" % "container" intransitive()

launcher in container <<= jetty in container
```

*sbt console:*

```
> container:start
```

**Launch with Tomcat**

*build.sbt:*

```scala
libraryDependencies += "com.github.jsimone" % "webapp-runner" % "7.0.34.1" % "container" intransitive()

launcher in container <<= tomcat in container
```

*sbt console:*

```
> container:start
```

**Triggered (re)launch**

*sbt console:*

```
> ~container:start
```

**Add an additional source directory**

Scala files in a source directory are compiled, and bundled in the project 
artifact *.jar* file.

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional source directory
unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

**Add an additional resource directory**

Files in a resource directory are not compiled, and are bundled directly in the 
project artifact *.jar* file.

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional resource directory
unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

**Change the default Web application resources directory**

The Web application resources directory is where static Web content (including 
*.html*, *.css*, and *.js* files, the *web.xml* container configuration file, 
etc.  By default, this is kept in *<project>/src/main/webapp*.

*build.sbt:*

```scala
// set <project>/src/main/WebContent as the webapp resources directory
webappSrc in webapp <<= (sourceDirectory in Compile) map  { _ / "WebContent" }
```

**Change the default Web application destination directory**

The Web application destination directory is where the static Web content, 
compiled Scala classes, library *.jar* files, etc. are placed.  By default, 
they go to *<project>/target/webapp*.

*build.sbt:*

```scala
// set <project>/target/WebContent as the webapp destination directory
webappDest in webapp <<= target map  { _ / "WebContent" }
```

**Modify the contents of the prepared Web application**

*build.sbt:*

```scala
// add an html file to the root of the prepared Web application
postProcess in webapp := {
  webappDir =>
    val fooHtml = new java.io.File(webappDir, "foo.html")
    val writer = new java.io.FileWriter(fooHtml)
    writer.write("""<html><body>foo</body></html>""")
    writer.close
}
```

**Use *WEB-INF/classes* instead of *WEB-INF/lib***

```scala
webInfClasses in webapp := true
```

**Prepare the Web application for execution and deployment**

*sbt console:*

```
webapp:prepare
```

**Package the Web application as a *.war* file**

*sbt console:*

```
package
```
