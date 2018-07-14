This is a simple Frege-based J2EE Web application to demonstrate using
[sbt-frege] with [xsbt-web-plugin].

## Usage

Start the service:

```
$ sbt
> jetty:quickstart
```

Try it out:

```
$ curl localhost:8080
<a href="hello">hello</a>
```

```
$ curl localhost:8080/hello
Hello, world!
```

```
$ curl localhost:8080/foo
404'd!
```

[sbt-frege]: https://github.com/earldouglas/sbt-frege
[xsbt-web-plugin]: https://github.com/earldouglas/xsbt-web-plugin
