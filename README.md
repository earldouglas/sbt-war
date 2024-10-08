[![Build status](https://github.com/earldouglas/sbt-war/workflows/build/badge.svg)](https://github.com/earldouglas/sbt-war/actions)
[![Latest version](https://img.shields.io/github/tag/earldouglas/sbt-war.svg)](https://index.scala-lang.org/earldouglas/sbt-war)

# sbt-war

sbt-war is an [sbt](https://www.scala-sbt.org/) plugin for building Web
apps with [servlets](https://en.wikipedia.org/wiki/Java_servlet).

sbt-war is formerly known as xsbt-web-plugin.  For documentation and
source code of prior versions, browse this repository from the desired
git tag.  The most recent prior version is
[4.2.5](https://github.com/earldouglas/sbt-war/tree/4.2.5).

## Features

* Package your project as a *.war* file
* Run your project in a Tomcat container

## Requirements

* sbt 1.x and up
* Scala 2.12.x and up

## Getting help

* Submit a question, bug report, or feature request as a [new GitHub
  issue](https://github.com/earldouglas/sbt-war/issues/new)
* Look for *earldouglas* in the `#sbt` channel on the [Scala Discord
  server](https://discord.com/invite/scala)

## Usage

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
addSbtPlugin("com.earldouglas" % "sbt-war" % "5.0.0-M1")
```

*build.sbt:*

```scala
scalaVersion := "3.5.1"
libraryDependencies += "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0" % Provided
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

## Settings and commands

| Setting Key                     | Type               | Default           | Notes                                                                                   |
| ------------------------------- | ------------------ | ----------------- | --------------------------------------------------------------------------------------- |
| `webappResources`               | `Map[String,File]` | *src/main/webapp* | Static files (HTML, CSS, JS, images, etc.) to serve directly                            |
| `webappClasses`                 | `Map[String,File]` | project classes   | .class files to copy into the *WEB-INF/classes* directory                               |
| `webappLib`                     | `Map[String,File]` | project libs      | .jar files to copy into the *WEB-INF/lib* directory                                     |
| `webappRunnerVersion`           | `String`           | `"10.1.28.0"`     | The version of `com.heroku:webapp-runner` to use for running the webapp                 |
| `webappComponentsRunnerVersion` | `String`           | `"10.1.28.0-M1"`  | The version of `com.earldouglas:webapp-components-runner` to use for running the webapp |
| `webappPort`                    | `Int`              | `8080`            | The local container port to use when running with `webappStart`                         |
| `warPort`                       | `Int`              | `8080`            | The local container port to use when running with `warStart`                            |
| `webappForkOptions`             | `ForkOptions`      | `ForkOptions()`   | Options for the forked JVM used when running with `webappStart`                         |
| `warForkOptions`                | `ForkOptions`      | `ForkOptions()`   | Options for the forked JVM used when running with `warStart`                            |

| Task Key      | Notes                                                                   |
| ------------- | ----------------------------------------------------------------------- |
| `warStart`    | Starts a local container, serving content from the packaged .war file   |
| `warJoin`     | Blocks until the container shuts down                                   |
| `warStop`     | Shuts down the container                                                |
| `webappStart` | Starts a local container, serving content directly from project sources |
| `webappJoin`  | Blocks until the container shuts down                                   |
| `webappStop`  | Shuts down the container                                                |

### `war` vs. `webapp`

Settings and commands that begin with `war` apply to the packaged .war
file, which includes resources, classes, and libraries. The development
cycle can be sped up by serving resources, classes, and libraries
directly from source, avoiding the overhead of packaging a
*.war* file.

Use the `webapp` prefix in place of `war` to skip packaging, and run the
container directly from source:

```
> webappStart
```

### `webappResources`

Webapp resources are the various static files, deployment descriptors,
etc. that go into a .war file.

The `webappResources` setting is a mapping from destination to source of
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

The `webappResources` mapping would look like this:

```
"index.html" -> File(".../src/main/webapp/index.html")
"styles/theme.css" -> File(".../src/main/webapp/styles/theme.css")
"WEB-INF/web.xml" -> File(".../src/main/webapp/WEB-INF/web.xml")
```

To use a different directory, e.g. *src/main/WebContent*:

```scala
webappResources :=
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

### `webappClasses`

By default, project classes are copied into the *WEB-INF/classes*
directory of the *.war* file.  To package them in a *.jar* file in the
*WEB-INF/lib* directory instead, set `exportJars`:

```scala
exportJars := true
```

See ["Configure
packaging"](https://www.scala-sbt.org/1.x/docs/Howto-Package.html) in
the sbt documentation for additional information.

### `webappLib`

By default, all runtime dependencies are copied into the *WEB-INF/lib*
directory.

To use a dependency at compile time but exclude it from the .war file,
set its scope to `Provided`:

```scala
libraryDependencies += "foo" % "bar" % "1.0.0" % Provided
```

### `webappRunnerVersion`

By default, [Webapp Runner](https://github.com/heroku/webapp-runner)
10.1.x is used to run the .war file in a forked JVM.  To use a different
version, set `webappRunnerVersion`:

```scala
webappRunnerVersion := "9.0.93.0"
```

### `webappComponentsRunnerVersion`

By default, [Webapp Components
Runner](https://github.com/earldouglas/webapp-components-runner) 10.1.x
is used to run the webapp in a forked JVM.  To use a different version,
set `webappComponentsRunnerVersion`:

```scala
webappComponentsRunnerVersion := "9.0.93.0.0"
```

### `warPort` and `webappPort`

By default, the container runs on port *8080*.  To use a different port,
set `warPort`/`webappPort`:

```scala
warPort := 9090
```

```scala
webappPort := 9090
```

### `warForkOptions`

To set environment variables, system properties, and more for the
forked container JVM, set a
[ForkOptions](https://www.scala-sbt.org/1.x/api/sbt/ForkOptions.html)
instance via `warForkOptions`.

For example: to attach a debugger, set `-Xdebug` and `-Xrunjdwp`:

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

### `webappForkOptions`

To set environment variables, system properties, and more for the
forked container JVM, set a
[ForkOptions](https://www.scala-sbt.org/1.x/api/sbt/ForkOptions.html)
instance via `webappForkOptions`.

For example: to attach a debugger, set `-Xdebug` and `-Xrunjdwp`:

*build.sbt:*

```scala
webappForkOptions :=
  ForkOptions()
    .withRunJVMOptions(
      Seq(
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
      )
    )
```

### `warStart` and `webappStart`

```
> warStart
```

```
> webappStart
```

### `warJoin` and `webappJoin`

To block sbt while the container is running, use `warJoin`/`webappJoin`:

```
$ sbt warStart warJoin
```

```
$ sbt webappStart webappJoin
```

This is useful for running sbt in production (e.g. in a Docker
container), if you're into that kind of thing.

### `warStop` and `webappStop`

Stop the running container with `warStop`/`webappStop`:

```
> warStop
```

```
> webappStop
```
