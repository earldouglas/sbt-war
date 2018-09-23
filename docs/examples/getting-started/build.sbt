scalaVersion := "2.12.5"
enablePlugins(JettyPlugin)
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
webXmlServlets += WebXmlServlet("GettingStartedServlet", "/*")
