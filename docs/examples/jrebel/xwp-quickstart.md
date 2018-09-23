% Faster turnaround with xsbt-web-plugin
% James Earl Douglas
% January 29, 2017

## Quick, start!

The [2.3.0 release][2.3.0] of [xsbt-web-plugin][xsbt-web-plugin]
introduced the [`quickstart`][quickstart] command, which speeds up the
container bootstrapping process by serving directly from a project's
static resources and compiled classes.

```
$ sbt
> jetty:quickstart
```

Not only does this skip the preparation and packaging of a *.war* file,
but it also allows the container to serve changes the developer makes to
static resources without recompiling any code and without restarting the
server.

## Reloading code changes instantly

We can take this a step further.  Serving changes to source code still
requires restarting the server, since its JVM process won't know about
changes to compiled classes.  We can get around this by using
[JRebel][jrebel] to reload changes directly in our container's JVM
process.

All we need to do is include in *build.sbt* an `-agentpath` argument
pointing to the JRebel library when we launch the container JVM:

```scala
javaOptions in Jetty += "-agentpath:/path/to/jrebel/lib/libjrebel64.so"
```

Optionally, extract the JRebel path to an environment variable, use
`sys.env`:

```scala
javaOptions in Jetty += s"""-agentpath:${sys.env("JREBEL")}/lib/libjrebel64.so"""
```

Now, when we launch our container using `quickstart`, we can re-issue
the `compile` command to see changes to source code deployed instantly,
with no need to restart the server.

```
$ sbt
> jetty:quickstart
> ~compile
```

Here's how the workflow looks on my (eight-year-old) laptop:

![`quickstart` screencast][screencast]

## Installing JRebel

1. Download and extract [JRebel "standalone"][standalone]

1. If you don't already have a license, request an [evaluation
   license][getkey], then check your email inbox for a *jrebel.lic*
   attachment

1. Activate JRebel using your license

    ```
    $ /path/to/jrebel/bin/active.sh /path/to/jrebel.lic
    ```

[2.3.0]: https://github.com/earldouglas/xsbt-web-plugin/blob/master/notes/2.3.0.markdown
[getkey]: https://zeroturnaround.com/software/jrebel/trial/getkey/
[jrebel]: https://zeroturnaround.com/software/jrebel/
[quickstart]: https://github.com/earldouglas/xsbt-web-plugin/blob/2.3.0/docs/2.3.md#quickstart-mode
[screencast]: xwp-quickstart/screencast.gif
[standalone]: https://zeroturnaround.com/software/jrebel/download/#standalone
[xsbt-web-plugin]: https://github.com/earldouglas/xsbt-web-plugin
