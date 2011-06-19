seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % (if(new File("jetty-conf") exists) "jetty" else "test")
