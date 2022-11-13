enablePlugins(ContainerPlugin)

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

containerLibs in Container :=
  Seq("fish.payara.extras" % "payara-micro" % "5.2020.3")

containerLaunchCmd in Container := { (port, path) =>
  Seq(
    "fish.payara.micro.PayaraMicro",
    "--deploy",
    path,
    "--contextroot",
    "/"
  )
}
