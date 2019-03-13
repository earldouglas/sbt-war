ThisBuild / organization := "local.test"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.8"

lazy val `payara-micro` = (project in file("."))
  .enablePlugins(ContainerPlugin)
  .settings(
    // enable this line only if you want to debug
    javaOptions in Container ++= Seq("-Xdebug", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"),
    libraryDependencies ++= Seq(
      microprofile,
      servlet
    ),
    containerLibs in Container := Seq(
      "fish.payara.extras" % "payara-micro" % "5.191"
    ),
    containerLaunchCmd in Container := { (port, path) =>
      Seq("fish.payara.micro.PayaraMicro", "--deploy", "target/webapp", "--contextroot", "/")
    }
  )

lazy val microprofile = {
  sys.props += "packaging.type" -> "jar"
  "org.eclipse.microprofile" % "microprofile" % "2.2" % "provided" pomOnly()
}

lazy val servlet = "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
