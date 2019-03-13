enablePlugins(ContainerPlugin)

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.0"

containerLibs in Container :=
  Seq("fish.payara.extras" % "payara-micro" % "5.191")

containerLaunchCmd in Container := { (port, path) =>
  Seq("fish.payara.micro.PayaraMicro", "--deploy", "target/webapp", "--contextroot", "/")
}
