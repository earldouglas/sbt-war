name := "test"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(JettyPlugin)

containerForkOptions := com.earldouglas.xwp.Compat.forkOptionsWithRunJVMOptions(Seq("-Dh2g2=42"))
