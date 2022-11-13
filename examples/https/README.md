# HTTPS with Tomcat

Let's serve our project over HTTPS directly from sbt.

## Creating a certificate

Create a basic self-signed certificate, KeyStore, and TrustStore by
following the steps [in this tutorial][server-cert-tutorial].  By
convention, passwords throughout will be `changeit`.

[server-cert-tutorial]: https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/index.html)

```
$ keytool -genkey -alias server-alias -keyalg RSA -keypass changeit \
          -storepass changeit -keystore keystore.jks
What is your first and last name?
  [Unknown]:
What is the name of your organizational unit?
  [Unknown]:
What is the name of your organization?
  [Unknown]:
What is the name of your City or Locality?
  [Unknown]:
What is the name of your State or Province?
  [Unknown]:
What is the two-letter country code for this unit?
  [Unknown]:
Is CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
  [no]:  yes
```

```
$ keytool -export -alias server-alias -storepass changeit \
          -file server.cer -keystore keystore.jks
Certificate stored in file <server.cer>
```

```
$ keytool -import -v -trustcacerts -alias server-alias \
          -file server.cer -keystore cacerts.jks -keypass changeit \
          -storepass changeit
Trust this certificate? [no]:  yes
Certificate was added to keystore
[Storing cacerts.jks]
```

## Configuring xsbt-web-plugin

We'll use Tomcat:

```scala
enablePlugins(TomcatPlugin)
```

The Tomcat plugin uses [webapp-runner], which allows us to enable SSL
via `--enable-ssl`:

[webapp-runner]: https://github.com/jsimone/webapp-runner

```scala
containerArgs := Seq(
  "--enable-ssl"
)
```

To enable SSL, we need to point the JVM toward our KeyStore, TrustStore,
and proivde the corresponding passwords:

```scala
javaOptions in Tomcat ++= Seq(
  "-Djavax.net.ssl.keyStore=keystore.jks",
  "-Djavax.net.ssl.keyStorePassword=changeit",
  "-Djavax.net.ssl.trustStore=cacerts.jks",
  "-Djavax.net.ssl.trustStorePassword=changeit"
)
```

By default, xsbt-web-plugin binds our project to port 8080, but let's
use something more HTTPS-ish instead:

```scala
containerPort := 8443
```

Now we can run our project from sbt with `tomcat:start`, and view it at
`https://localhost:8443/`.  Since we used a self-signed certificate,
we may need to reassure our Web browser that it's safe to proceed.

```
$ curl -ik https://localhost:8443
HTTP/1.1 200
Content-Type: text/html;charset=UTF-8
Content-Length: 60
Date: Wed, 06 Jun 2018 18:48:42 GMT

<html>
  <body>
    <h1>Hello, world!</h1>
  </body>
</html>
```

## Testing

```
$ sbt Tomcat/test
[info] starting server ...
[info] TestSuite:
[info] - /
[info] Run completed in 1 second, 806 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 5 s, completed Nov 13, 2022 2:03:45 PM
[info] waiting for server to shut down...
```
