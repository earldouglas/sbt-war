# Getting started with xsbt-web-plugin

This project shows how to build a basic Scala Web application using sbt
and [xsbt-web-plugin][1].  To get started, either clone this project or
follow the steps below to recreate it.

[1]: https://github.com/earldouglas/xsbt-web-plugin

## Starting from scratch

Create a new empty project:

```
mkdir xwp-template
cd xwp-template
```

Set up the project structure:

```
mkdir project
mkdir -p src/main/scala
mkdir -p src/main/webapp/WEB-INF
```

Configure sbt:

*project/build.properties*:

```
sbt.version=0.13.8
```

*project/plugins.sbt*:
```
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.5")
```

*build.sbt*:
```
name := "xwp-template"

organization := "com.earldouglas"

scalaVersion := "2.11.7"

enablePlugins(JettyPlugin)

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
```

Add a servlet:

*src/main/scala/XwpTemplateServlet.scala*:

```scala
package com.earldouglas.xwptemplate

import javax.servlet.http.HttpServlet

class XwpTemplateServlet extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {

    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val responseBody: String =
      """<html>
        |  <body>
        |    <h1>Hello, world!</h1>
        |    <a href="/">home</a>
        |  </body>
        |</html>""".stripMargin
    response.getWriter.write(responseBody)
  }
}
```

*src/main/webapp/WEB-INF/web.xml*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
           http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

  <servlet>
    <servlet-name>xwp template</servlet-name>
    <servlet-class>com.earldouglas.xwptemplate.XwpTemplateServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>xwp template</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>

</web-app>
```

## Launching from sbt

From sbt, run the command `jetty:start`:

```
> jetty:start
[info] jetty-9.1.0.v20131115
[info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
[info] Started SelectChannelConnector@0.0.0.0:8080
[success] Total time: 0 s, completed May 27, 2013 11:29:14 AM
>
```

The Web application is now running at *http://localhost:8080/*.  Take a look with a Web browser, or via curl:

```
$ curl -i localhost:8080
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8
Content-Length: 48
Server: Jetty(6.1.22)

<html><body><h1>Hello, world!</h1></body></html>
```

## Deploying to a servlet container

To build a WAR file suitable for deployment, run the command `package` from sbt:

```
> package
[success] Total time: 0 s, completed May 27, 2013 11:31:59 AM
> 
```

The WAR file can be found in *target/scala-2.11/xwp-template_2.11-0.1.0-SNAPSHOT.war*.
