## jetty.xml location

Use jettyConfFiles, like so:

```scala
env in Compile := Some(file(".") / "conf" / "jetty" / "jetty-env.xml" asFile)
```
## Error scenarios

### Object is not of type class org.eclipse.jetty.server.Server

This error can be expected by those migrating from maven-jetty-plugin to sbt. The relevant part of the error message is:

    java.lang.IllegalArgumentException: Object is not of type class org.eclipse.jetty.server.Server
      at org.eclipse.jetty.xml.XmlConfiguration.configure(XmlConfiguration.java:203)
      at org.eclipse.jetty.plus.webapp.EnvConfiguration.configure(EnvConfiguration.java:118)
      ...

This may be caused by the start of the jetty.xml file. Maven requires a server tag that refers to the above class. xsbt-web-plugin needs the following instead:

```xml
<Configure id="Server" class="org.eclipse.jetty.webapp.WebAppContext">
```
