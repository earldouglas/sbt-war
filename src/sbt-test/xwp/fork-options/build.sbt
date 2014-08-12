name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

jetty(options = new ForkOptions(runJVMOptions = Seq("-Dh2g2=42")))
