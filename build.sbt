// General
enablePlugins(SbtPlugin)
name := "xsbt-web-plugin"
organization := "com.earldouglas"
sbtPlugin := true
scalacOptions ++= Seq("-feature", "-deprecation")
scalaVersion := "2.12.20"

// scripted-plugin
scriptedBufferLog := false
watchSources ++= { (sourceDirectory.value ** "*").get }

// AWS deployment support
val awsJavaSdkVersion = "1.12.772"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % awsJavaSdkVersion
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % awsJavaSdkVersion

// Scalafix
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision
scalacOptions += "-Ywarn-unused-import"

// webapp-runner
lazy val webappRunnerVersion =
  settingKey[String]("webapp-runner version")
webappRunnerVersion := "9.0.68.1"
libraryDependencies += "com.heroku" % "webapp-runner" % webappRunnerVersion.value intransitive ()

// sbt-buildinfo
enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](webappRunnerVersion)
buildInfoPackage := "com.earldouglas.sbt.war"

// Testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
Test / fork := true

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
ThisBuild / versionScheme := Some("semver-spec")
