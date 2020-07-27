scalaVersion := "2.13.3"

enablePlugins(ContainerPlugin)

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

containerLibs in Container :=
  Seq("fish.payara.extras" % "payara-micro" % "5.201")

containerLaunchCmd in Container := { (port, path) =>
  Seq("fish.payara.micro.PayaraMicro", "--deploy", "target/webapp", "--contextroot", "/")
}
