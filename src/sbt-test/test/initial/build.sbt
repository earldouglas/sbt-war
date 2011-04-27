seq(WebPlugin.webSettings :_*)

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
  "ch.qos.logback" % "logback-classic" % "0.9.26"
)
