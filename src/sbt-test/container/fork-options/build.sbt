name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerForkOptions := new ForkOptions(runJVMOptions = Seq("-Dh2g2=42"))
