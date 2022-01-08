// General
crossSbtVersions := Seq("0.13.18", "1.5.4")
Global / excludeLintKeys += crossSbtVersions // don't warn about unused setting
enablePlugins(SbtPlugin)
name := "xsbt-web-plugin"
organization := "com.earldouglas"
sbtPlugin := true
scalacOptions ++= Seq("-feature", "-deprecation")
scalaVersion := "2.12.15"

// scripted-plugin
scriptedBufferLog := false
watchSources ++= { (sourceDirectory.value ** "*").get }

// AWS deployment support
libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % "1.12.136"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.136"

// Publish to Sonatype, https://www.scala-sbt.org/release/docs/Using-Sonatype.html
credentials := List(
  Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
)
description := "Servlet support for sbt"
developers := List(
  Developer(
    id = "earldouglas",
    name = "James Earl Douglas",
    email = "james@earldouglas.com",
    url = url("https://earldouglas.com/")
  )
)
homepage := Some(url("https://github.com/earldouglas/xsbt-web-plugin"))
licenses := List(
  "BSD New" -> url("https://opensource.org/licenses/BSD-3-Clause")
)
organizationHomepage := Some(url("https://earldouglas.com/"))
organizationName := "James Earl Douglas"
pomIncludeRepository := { _ => false }
publishMavenStyle := true
publishTo := Some(
  "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
)
scmInfo := Some(
  ScmInfo(
    url("https://github.com/earldouglas/xsbt-web-plugin"),
    "scm:git@github.com:earldouglas/xsbt-web-plugin.git"
  )
)
