Add Lift and Jetty to project dependencies:

```scala
libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
  "ch.qos.logback" % "logback-classic" % "0.9.26"
)
```
		
or in case if you want to use Jetty 7:

```scala
libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container",
  "ch.qos.logback" % "logback-classic" % "0.9.26"
)
```
