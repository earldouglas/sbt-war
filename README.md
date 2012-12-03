## Usage

Setup [SBT](http://github.com/harrah/xsbt/).

Add plugin to project in `project/plugins.sbt`:

```scala
libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.11"))
```

Inject plugin settings into project in `build.sbt`:

```scala
seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
```

## See [Wiki](http://github.com/JamesEarlDouglas/xsbt-web-plugin/wiki/) for details.

## License
This software is distributed under modified 3-clause BSD license. See [LICENSE](https://github.com/JamesEarlDouglas/xsbt-web-plugin/blob/master/LICENSE) for more information.
