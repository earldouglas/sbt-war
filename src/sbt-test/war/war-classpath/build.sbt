seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"

fullClasspath in Runtime in packageWar <+= baseDirectory.map(bd => bd / "extras")

port in container.Configuration := 7129
