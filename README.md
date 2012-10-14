Old version moved to [0.1](https://github.com/siasia/xsbt-web-plugin/tree/0.1)

# Usage

Setup [SBT](http://github.com/harrah/xsbt/).

Add plugin to project in `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.siasia" % "xsbt-web-plugin" % "0.2.11.1")
```

Inject plugin settings into project in `build.sbt`:

```scala
seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
```
		
# See [Wiki](http://github.com/siasia/xsbt-web-plugin/wiki/) for details

# License
This software is distributed under modified 3-clause BSD license. See [LICENSE](https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE) for more information.
