seq(WebPlugin.webSettings :_*)

libraryDependencies ++= Seq(
	"net.liftweb" %% "lift-webkit" % "2.3" % "compile",
	"org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty",
	"javax.servlet" % "servlet-api" % "2.5" % "provided",
	"ch.qos.logback" % "logback-classic" % "0.9.26"
)
