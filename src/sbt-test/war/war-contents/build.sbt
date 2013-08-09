name := "war-contents"

version := "1.2.3"

seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"

port in container.Configuration := 7130

