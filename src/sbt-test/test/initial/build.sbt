seq(WebPlugin.webSettings :_*)

resolvers += "Local Repo" at "file://"+ Path.userHome + "/.m2/repository"

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty",
  "ch.qos.logback" % "logback-classic" % "0.9.26"
)
