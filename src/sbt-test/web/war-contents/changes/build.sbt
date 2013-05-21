name := "war-contents"

version := "1.2.3"

seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % (if(!(new File("jetty-conf") exists)) "container" else "test")

fullClasspath in Runtime in packageWar <+= baseDirectory.map(bd => bd / "extras")

port in container.Configuration := 7130

classesAsJar in Compile := true
