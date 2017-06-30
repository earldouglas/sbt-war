# Getting started with xsbt-web-plugin

## Starting from scratch

Create a new empty project:

```
$ mkdir getting-started
$ cd getting-started
```

Set up the project structure:

```
$ mkdir project
$ mkdir -p src/main/scala
$ mkdir -p src/main/webapp/WEB-INF
```

Configure sbt:

*project/build.properties*:

```
sbt.version=0.13.13
```

*project/build.sbt*:

```
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "3.0.3")
```

*build.sbt*:

```
scalaVersion := "2.12.1"
enablePlugins(JettyPlugin)
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
```

Add a servlet:

*src/main/scala/GettingStartedServlet.scala*:

```scala
class GettingStartedServlet extends javax.servlet.http.HttpServlet {

  override def doGet( req: javax.servlet.http.HttpServletRequest
                    , res: javax.servlet.http.HttpServletResponse
                    ) {

    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")

    val responseBody: String =
      """<html>
        |  <body>
        |    <h1>Hello, world!</h1>
        |  </body>
        |</html>""".stripMargin
    res.getWriter.write(responseBody)
  }
}
```

*src/main/webapp/WEB-INF/web.xml*:

```xml
<web-app>

  <servlet>
    <servlet-name>getting started</servlet-name>
    <servlet-class>GettingStartedServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>getting started</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>

</web-app>
```

## Launching from sbt

From sbt, run the command `jetty:start`:

```
> jetty:start
2017-02-20 10:17:10.331:INFO:oejs.Server:main: Started @1296ms
```

The container is now running at *http://localhost:8080*:

```
$ curl -i localhost:8080
HTTP/1.1 200 OK
Date: Mon, 20 Feb 2017 17:18:24 GMT
Content-Type: text/html;charset=utf-8
Content-Length: 85
Server: Jetty(9.4.1.v20170120)

<html>
  <body>
    <h1>Hello, world!</h1>
  </body>
</html>
```

## Deploying to a servlet container

To build a WAR file suitable for deployment, run the command `package`
from sbt:

```
> package
[success] Total time: 1 s, completed Feb 20, 2017 10:18:47 AM
```

The *.war* file is named *getting-started_2.12-0.1-SNAPSHOT.war*, and
can be found in *getting-started/target/scala-2.12/*.
