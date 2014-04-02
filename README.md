[![Build Status](https://travis-ci.org/earldouglas/xsbt-web-plugin.png?branch=master)](https://travis-ci.org/earldouglas/xsbt-web-plugin)

xsbt-web-plugin is an extension to [sbt](http://www.scala-sbt.org/) for building enterprise Web applications based on the [Java J2EE Servlet specification](http://en.wikipedia.org/wiki/Java_Servlet).

xsbt-web-plugin supports both Scala and Java, and is best suited for projects that:

* Deploy to common cloud platforms (e.g. [Google App Engine](https://developers.google.com/appengine/), [Heroku](https://www.heroku.com/), [Elastic Beanstalk](https://console.aws.amazon.com/elasticbeanstalk/home), [Jelastic](http://jelastic.com/))
* Deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish, WebSphere)
* Incorporate J2EE libraries (e.g. [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages), [JSF](http://en.wikipedia.org/wiki/JavaServer_Faces), [EJB](http://en.wikipedia.org/wiki/Ejb))
* Utilize J2EE technologies (e.g. [`Servlet`](http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html)s, [`Filter`](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html)s, [JNDI](http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface))
* Have a specific need to be packaged as a [*.war* file](https://en.wikipedia.org/wiki/WAR_%28Sun_file_format%29)

## Getting started 

The quickest way to get started is to clone the [xwp-template](https://github.com/JamesEarlDouglas/xwp-template) project, which sets up the necessary directories, files, and configuration for a basic xsbt-web-plugin project.

For more information, please see the [wiki](http://github.com/earldouglas/xsbt-web-plugin/wiki/).

## Quick reference

First, add xsbt-web-plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.8.0")
```

For [*.sbt* build definitions](http://www.scala-sbt.org/release/docs/Getting-Started/Basic-Def.html), inject the plugin settings in *build.sbt*:

```scala
seq(webSettings :_*)
```

For [*.scala* build definitions](http://www.scala-sbt.org/release/docs/Getting-Started/Full-Def.html), inject the plugin settings in *Build.scala*:

```scala
import com.earldouglas.xsbtwebplugin.WebPlugin

Project(..., settings = Project.defaultSettings ++ WebPlugin.webSettings)
```

Add a Servlet API to the *provided* configuration:

```scala
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
```

Include either Jetty or Tomcat in the *container* configuration:

*Jetty:*

```scala
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container"
)
```

*Tomcat:*

```scala
libraryDependencies ++= Seq(
  "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % "container"
)
```
