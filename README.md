[![Build status](https://github.com/earldouglas/sbt-war/workflows/build/badge.svg)](https://github.com/earldouglas/sbt-war/actions)
[![Latest version](https://img.shields.io/github/v/tag/earldouglas/sbt-war)](https://github.com/earldouglas/sbt-war/tags)
[![Maven Central](https://img.shields.io/maven-central/v/com.earldouglas/sbt-war_2.12_1.0)](https://repo1.maven.org/maven2/com/earldouglas/sbt-war_2.12_1.0/)

# sbt-war

sbt-war is an [sbt](https://www.scala-sbt.org/) plugin for packaging and
running .war files.

sbt-war is formerly known as xsbt-web-plugin.  For documentation and
source code of prior versions, browse this repository from the desired
git tag.  The most recent prior version is
[4.2.5](https://github.com/earldouglas/sbt-war/tree/4.2.5).


## Requirements

* sbt 1.x and up
* Scala 2.12.x and up

## Getting help

* Submit a question, bug report, or feature request as a [new GitHub
  issue](https://github.com/earldouglas/sbt-war/issues/new)
* Look for *earldouglas* in the `#sbt` channel on the [Scala Discord
  server](https://discord.com/invite/scala)

## Getting started from a template

```
$ sbt new earldouglas/sbt-war.g8

name [My Web Project]: hello sbt-war

Template applied in ./hello-sbt-war

$ cd hello-sbt-war/
$ sbt
> warStart
```

```
$ curl localhost:8080/hello
<h1>Hello, world!</h1>
```

```
> warStop
```

## Getting started from scratch

Create a new empty project:

```
$ mkdir myproject
$ cd myproject
```

Set up the project structure:

```
$ mkdir project
$ mkdir -p src/main/scala/mypackage
```

Configure sbt:

*project/build.properties:*

```
sbt.version=1.10.2
```

*project/plugins.sbt:*

```scala
addSbtPlugin("com.earldouglas" % "sbt-war" % "5.0.0-M4")
```

*build.sbt:*

```scala
scalaVersion := "3.5.1"
enablePlugins(SbtWar)
```

Add a servlet:

*src/main/scala/mypackage/MyServlet.scala*:

```scala
package mypackage

import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = Array("/hello"))
class MyServlet extends HttpServlet:
  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit =
    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")
    res.getWriter.write("""<h1>Hello, world!</h1>""")
```

Run it from sbt with `warStart`:

```
$ sbt
> warStart
```

```
$ curl localhost:8080/hello
<h1>Hello, world!</h1>
```

Stop it with `warStop`:

```
> warStop
```

Create a .war file with `package`:

```
> package
```

## Settings

| Key                | Type               | Default            | Notes                                                                       |
| ------------------ | ------------------ | ------------------ | --------------------------------------------------------------------------- |
| `warResources`     | `Map[String,File]` | *src/main/webapp*  | Static files (HTML, CSS, JS, images, etc.) to serve directly                |
| `warClasses`       | `Map[String,File]` | project classes    | .class files to copy into the *WEB-INF/classes* directory                   |
| `warLib`           | `Map[String,File]` | project libs       | .jar files to copy into the *WEB-INF/lib* directory                         |
| `warPort`          | `Int`              | `8080`             | The local container port to use when running with `warStart`                |
| `warForkOptions`   | [`ForkOptions`]    | [`BufferedOutput`] | Options for the forked JVM used when running with `warStart`                |

## Commands

| Key             | Notes                                                                   |
| --------------- | ----------------------------------------------------------------------- |
| `warStart`      | Starts a local container, serving content from the packaged .war file   |
| `warQuickstart` | Starts a local container, serving content directly from project sources |
| `warJoin`       | Blocks until the container shuts down                                   |
| `warStop`       | Shuts down the container                                                |

### `warResources`

Resources are the various static files, deployment descriptors, etc.
that go into a .war file.

The `warResources` setting is a mapping from destination to source of
these files.  The destination is a path relative to the contents of the
.war file.  The source is a path on the local filesystem.

By default, everything in *src/main/webapp* is included.

For example, given the following .war file:

```
myproject.war
├── index.html
├── styles/
│   └── theme.css
├── WEB-INF/
│   └── web.xml
└── META-INF/
    └── MANIFEST.MF
```

The `warResources` mapping would look like this:

```
"index.html" -> File(".../src/main/webapp/index.html")
"styles/theme.css" -> File(".../src/main/webapp/styles/theme.css")
"WEB-INF/web.xml" -> File(".../src/main/webapp/WEB-INF/web.xml")
```

To use a different directory, e.g. *src/main/WebContent*:

```scala
warResources :=
  (Compile / sourceDirectory)
    .map(_ / "WebContent")
    .map(WebappComponents.getResources)
```

Manifest attributes of the *.war* file can be configured via
`packageOptions`:

```scala
sbt.Keys.`package` / packageOptions +=
  Package.ManifestAttributes(
    java.util.jar.Attributes.Name.SEALED -> "true"
  )
```

### `warClasses`

By default, project classes are copied into the *WEB-INF/classes*
directory of the *.war* file.  To package them in a *.jar* file in the
*WEB-INF/lib* directory instead, set `exportJars`:

```scala
exportJars := true
```

See ["Configure
packaging"](https://www.scala-sbt.org/1.x/docs/Howto-Package.html) in
the sbt documentation for additional information.

### `warLib`

By default, all runtime dependencies are copied into the *WEB-INF/lib*
directory.

To use a dependency at compile time but exclude it from the .war file,
set its scope to `Provided`:

```scala
libraryDependencies += "foo" % "bar" % "1.0.0" % Provided
```

### `warPort`

By default, the container runs on port *8080*.  To use a different port,
set `warPort`:

```scala
warPort := 9090
```

### `warForkOptions`

To set environment variables, system properties, and more for the
forked container JVM, set a
[ForkOptions](https://www.scala-sbt.org/1.x/api/sbt/ForkOptions.html)
instance via `warForkOptions`.

For example: to be able to attach a debugger, set `-Xdebug` and
`-Xrunjdwp`:

*build.sbt:*

```scala
warForkOptions :=
  ForkOptions()
    .withRunJVMOptions(
      Seq(
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
      )
    )
```

### `warStart` and `warQuickstart`

To run the webapp, use `warStart`:

```
> warStart
```

To skip packaging the .war file before launching the container, use
`warQuickstart`:

```
> warQuickstart
```

### `warJoin`

To block sbt while the container is running, use `warJoin`:

```
$ sbt warStart warJoin
```

This is useful for running sbt in production (e.g. in a Docker
container), if you're into that kind of thing.

### `warStop`

To stop the running container, use `warStop`:

```
> warStop
```

[`ForkOptions`]: https://www.scala-sbt.org/1.x/api/sbt/ForkOptions.html
[`BufferedOutput`]: https://www.scala-sbt.org/1.x/api/sbt/OutputStrategy$$BufferedOutput.html
