enablePlugins(ContainerPlugin)

libraryDependencies += "jakarta.platform" % "jakarta.jakartaee-api" % "10.0.0" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"

Container / javaOptions ++=
  Seq(
    "--add-opens",
    "java.base/jdk.internal.loader=ALL-UNNAMED"
  )

Container / containerLibs :=
  Seq("fish.payara.extras" % "payara-micro" % "6.2024.2")

Container / containerLaunchCmd := { (port, path) =>
  Seq(
    "fish.payara.micro.PayaraMicro",
    "--deploy",
    path,
    "--contextroot",
    "/mycontext"
  )
}
