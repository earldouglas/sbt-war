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
sbt.version=0.13.0
```

*project/plugins.sbt*:
```
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.5.0")
```

*build.sbt*:
```
name := "xwp-template"

organization := "com.earldouglas"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.3"

seq(webSettings :_*)

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container"

libraryDependencies += "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "container"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"
```

Add a servlet:

*src/main/scala/XwpTemplateServlet.scala*:

```scala
package com.earldouglas.xwptemplate

import scala.xml.NodeSeq
import javax.servlet.http.HttpServlet

class XwpTemplateServlet extends HttpServlet {

  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpServletResponse

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {

    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")

    val responseBody: NodeSeq = <html><body><h1>Hello, world!</h1></body></html>
    response.getWriter.write(responseBody.toString)
  }
}
```

*src/main/webapp/WEB-INF/web.xml*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app
  xmlns="http://java.sun.com/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
  version="2.5"
  >

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

From sbt, run the command `container:start`:

```
> container:start
[info] jetty-9.1.0.v20131115
[info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
[info] Started SelectChannelConnector@0.0.0.0:8080
[success] Total time: 0 s, completed May 27, 2013 11:29:14 AM
>
```

The Web application is now running at http://localhost:8080/.  Take a look with a Web browser, or via curl:

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

The WAR file can be found in *target/scala-2.10/xwp-template_2.10-0.1.0-SNAPSHOT.war*.