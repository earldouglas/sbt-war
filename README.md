Old version moved to [0.1](https://github.com/siasia/xsbt-web-plugin/tree/0.1)

# Notice

This is a forked version of [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin) which **adds support for the Jetty SSL connector in xsbt-web-plugin** (so that the app can be deployed on both **http** and **https** ports).

This work has been sponsored and donated to the public by the **great Parkio company** - http://www.parkio.com

# Usage

```
rm -rf $HOME/.ivy2/cache/com.github.siasia
rm -rf $HOME/.ivy2/local/com.github.siasia
git clone https://github.com/ambisoft/xsbt-web-plugin.git
cd xsbt-web-plugin
sh <path_to_sbt>/sbt publish-local (builds a plugin and publishes locally in the ivy repository)
```

```scala
libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10.1"))
```

Inject plugin settings into project in `build.sbt`:

```scala
seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
```

## SSL-specific config
		
```
ssl in container.Configuration := Some(ssl_port, "path_to_keystore", "keystore_password", "key_password")
```

# See [Wiki](http://github.com/siasia/xsbt-web-plugin/wiki/) for details

# License
This software is distributed under modified 3-clause BSD license. See [LICENSE](https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE) for more information.
