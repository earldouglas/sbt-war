# Getting started with xsbt-web-plugin and payara-micro

## Starting from scratch

Create a new empty project:

```
$ mkdir payara-micro
$ cd payara-micro
```

Set up the project structure:

```
$ mkdir project
$ mkdir -p src/main/scala/local/test/endpoint
```

Configure sbt:

*project/build.properties*:

```
sbt.version=1.2.8
```

*project/plugins.sbt*:

```
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.2")
```

*build.sbt*:

```
enablePlugins(ContainerPlugin)

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.0"

containerLibs in Container :=
  Seq("fish.payara.extras" % "payara-micro" % "5.191")

containerLaunchCmd in Container := { (port, path) =>
  Seq("fish.payara.micro.PayaraMicro", "--deploy", "target/webapp", "--contextroot", "/")
}
```

Add an application class:

*src/main/scala/local/test/Main.scala*:

```scala
package local.test

import java.util

import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application
import local.test.endpoint.Hello

@ApplicationPath("/*")
class Main extends Application {

  override def getClasses: util.Set[Class[_]] = {
    val h = new util.HashSet[Class[_]]
    h.add(classOf[Hello])
    h
  }
}
```


Add an endpoint:

*src/main/scala/local/test/endpoint/Hello.scala*:

```scala
package local.test.endpoint

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces, QueryParam}

@Path("/hello")
class Hello {

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def getMessage(@QueryParam("name") name: String): Response = {
    val greeting = if (name == null || name.isEmpty) "Nobody" else name
    Response.ok("Hallo " + greeting + "\n").build
  }
}
```

## Launching from sbt

From sbt, run the command `container:start`:

```
> container:start
```

The container is now running at *http://localhost:8080*:

```
$ curl -i localhost:8080/hello
HTTP/1.1 200 OK
Server: Payara Micro #badassfish
Content-Type: text/plain
Content-Length: 13
X-Frame-Options: SAMEORIGIN

Hallo Nobody
```

## Deploying to a servlet container

To build a WAR file suitable for deployment, run the command `package`
from sbt:

```
> package
[success] Total time: 0 s, completed Apr 6, 2018 7:53:08 AM
```

The *.war* file is named *payara-micro_2.12-0.1.0-SNAPSHOT.war*, and
can be found in *payara-micro/target/scala-2.12/*.
