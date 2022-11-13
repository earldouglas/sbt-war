enablePlugins(ContainerPlugin)

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

containerLibs in Container :=
  Seq("fish.payara.extras" % "payara-micro" % "5.2020.7")

containerLaunchCmd in Container := { (port, path) =>
  Seq(
    "fish.payara.micro.PayaraMicro",
    "--deploy",
    path,
    "--contextroot",
    "/"
  )
}

test := (Container / test).value
