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
sbt.version=1.1.2
```

*project/build.sbt*:

```
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.2")
```

*build.sbt*:

```
scalaVersion := "2.12.5"
enablePlugins(JettyPlugin)
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
webXmlServlets += WebXmlServlet("GettingStartedServlet", "/*")
```

Add a servlet:

*src/main/scala/GettingStartedServlet.scala*:

```scala
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GettingStartedServlet extends HttpServlet {

  override def doGet( req: HttpServletRequest
                    , res: HttpServletResponse
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

## Launching from sbt

From sbt, run the command `jetty:start`:

```
> jetty:start
2018-04-06 07:52:39.210:INFO:oejs.Server:main: Started @656ms
```

The container is now running at *http://localhost:8080*:

```
$ curl -i localhost:8080
HTTP/1.1 200 OK
Date: Fri, 06 Apr 2018 13:52:54 GMT
Content-Type: text/html;charset=utf-8
Content-Length: 60
Server: Jetty(9.4.8.v20171121)

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
[success] Total time: 0 s, completed Apr 6, 2018 7:53:08 AM
```

The *.war* file is named *getting-started_2.12-0.1.0-SNAPSHOT.war*, and
can be found in *getting-started/target/scala-2.12/*.
