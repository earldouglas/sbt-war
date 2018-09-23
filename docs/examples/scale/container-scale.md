% Scaling out with xsbt-web-plugin
% James Earl Douglas
% February 12, 2017

Version 3.0.0 of [xsbt-web-plugin][1] introduces the
[`containerScale`][2] setting, which configures `<container>:start`,
`<container>:quickstart`, and `<container>:debug` to fire up multiple
instances of the container, and `<container>:stop` to tear them all
down.

Let's take a look at how to use `containerScale` to run ten instances of
our web app in parallel.

We'll use a simple, but slow, servlet:

```scala
class SlowServlet extends javax.servlet.http.HttpServlet {

  override def doGet( req: javax.servlet.http.HttpServletRequest
                    , res: javax.servlet.http.HttpServletResponse
                    ) {

    res.setContentType("text/plain")
    res.setCharacterEncoding("UTF-8")

    "Hello, world!\n" foreach { c =>
      Thread.sleep(25)
      res.getWriter.write(c)
      res.getWriter.flush()
    }

  }
}
```

To make sure our web app is nice and slow, we'll crank down the number
of threads available to the container:

*etc/jetty.xml:*

```xml
<?xml version="1.0"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN"
"http://www.eclipse.org/jetty/configure_9_0.dtd">

<Configure id="Server" class="org.eclipse.jetty.server.Server">

    <Get name="ThreadPool">
      <Set name="minThreads" type="int">
        <Property name="threads.min" default="4"/>
      </Set>
      <Set name="maxThreads" type="int">
        <Property name="threads.max" default="4"/>
      </Set>
    </Get>

</Configure>
```

We'll use a mostly standard sbt configuration, but we'll add
`containerConfigFile` to specify our thread-limited Jetty configuration
file, and `containerScale` to specify how many instances we want to work
with.

*build.sbt:*

```scala
scalaVersion := "2.12.1"

libraryDependencies +=
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

enablePlugins(JettyPlugin)

containerConfigFile := Some(file("etc/jetty.xml"))

containerScale := 10
```

*project/build.properties:*

```
sbt.version=0.13.13
```

*project/build.sbt:*

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "3.0.0")
```

Let's fire up our web app and try it out.

```
$ sbt
> jetty:quickstart
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[info] starting server ...
[success] Total time: 1 s, completed Feb 12, 2017 6:19:14 PM
>
```

We now have ten instances of our web app running on ports ranging from
8080 to 8089.  If we interact with one of them, it's pretty slow:

![Slow response is slow](container-scale/slow-response.gif)

A simple load test shows just how slow it is:

```
$ wrk -c 25 -d 5s http://localhost:8080/
Running 5s test @ http://localhost:8080/
  2 threads and 25 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   332.27ms    0.89ms 335.21ms   87.50%
    Req/Sec     2.62      0.50     3.00     62.50%
  16 requests in 5.01s, 3.73KB read
Requests/sec:      3.19
Transfer/sec:     763.19B
```

We can only complete about three requests per second through a single
instance.

Let's try load balancing across all ten instances.  To do so, we can
whip up a quick HAProxy configuration:

*haproxy/haproxy.cfg:*

```
global
  maxconn 4096
  daemon

defaults
  mode    http
  timeout connect 5000
  timeout client  5000
  timeout server  5000

frontend http
  bind *:80
  mode http
  default_backend mycluster

backend mycluster
  balance roundrobin
  server  mycluster0 172.17.0.1:8080 check
  server  mycluster1 172.17.0.1:8081 check
  server  mycluster2 172.17.0.1:8082 check
  server  mycluster3 172.17.0.1:8083 check
  server  mycluster4 172.17.0.1:8084 check
  server  mycluster5 172.17.0.1:8085 check
  server  mycluster6 172.17.0.1:8086 check
  server  mycluster7 172.17.0.1:8087 check
  server  mycluster8 172.17.0.1:8088 check
  server  mycluster9 172.17.0.1:8089 check
```

On my workstation, the Docker host IP address is `172.17.0.1`.

We can fire up HAProxy using this configuration via Docker:

```
$ docker run -p 8000:80 -v `pwd`/haproxy:/usr/local/etc/haproxy:ro haproxy:1.7
```

Load testing across the instances is about ten times faster:

```
$ wrk -c 25 -d 5s http://localhost:8000/
Running 5s test @ http://localhost:8000/
  2 threads and 25 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   619.35ms  344.04ms   1.98s    79.10%
    Req/Sec    20.97     16.38    70.00     81.08%
  175 requests in 5.02s, 43.11KB read
  Socket errors: connect 0, read 0, write 0, timeout 2
Requests/sec:     34.88
Transfer/sec:      8.59KB
```

We can complete over thirty requests per second through the load
balancer.

[1]: https://github.com/earldouglas/xsbt-web-plugin
[2]: https://github.com/earldouglas/xsbt-web-plugin#running-multiple-containers
