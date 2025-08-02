// General
ThisBuild / organization := "com.earldouglas"

// Scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalacOptions += "-Ywarn-unused-import"
ThisBuild / scalacOptions += s"-P:semanticdb:sourceroot:${baseDirectory.value}"

// Testing
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
ThisBuild / Test / fork := true

def warRunnerVersion(servletSpec: String) =
  Def.setting {
    version.value
      .split("-")
      .toList match {
      case v :: Nil => s"""${v}_${servletSpec}"""
      case v :: t   => s"""${v}_${servletSpec}-${t.mkString("-")}"""
      case _        => throw new Exception("wat")
    }
  }

lazy val warRunner_3_0 =
  project
    .in(file("runners/3.0"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("3.0").value,
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "11",
          "-target",
          "11",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.github.jsimone" % "webapp-runner" % "7.0.91.0"
    )

lazy val warRunner_3_1 =
  project
    .in(file("runners/3.1"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("3.1").value,
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "11",
          "-target",
          "11",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "8.5.68.1"
    )

lazy val warRunner_4_0 =
  project
    .in(file("runners/4.0"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("4.0").value,
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "11",
          "-target",
          "11",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "9.0.106.0"
    )

lazy val warRunner_6_0 =
  project
    .in(file("runners/6.0"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("6.0").value,
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "11",
          "-target",
          "11",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "10.1.43.0"
    )

lazy val sbtWar =
  project
    .in(file("."))
    .enablePlugins(SbtPlugin)
    .enablePlugins(BuildInfoPlugin)
    .settings(
      name := "sbt-war",
      sbtPlugin := true,
      scalacOptions ++= Seq("-feature", "-deprecation"),
      scalaVersion := "2.12.18",
      //
      // scripted-plugin
      scriptedBufferLog := false,
      watchSources ++= { (sourceDirectory.value ** "*").get },
      scriptedLaunchOpts += "-DtemplateDirectory=" + (sourceDirectory.value / "test" / "template"),
      scriptedBatchExecution := true,
      scriptedParallelInstances := 8,
      //
      // sbt-buildinfo
      buildInfoPackage := "com.earldouglas.sbt.war",
      buildInfoKeys := Seq[BuildInfoKey](version)
    )
    .aggregate(
      warRunner_3_0,
      warRunner_3_1,
      warRunner_4_0,
      warRunner_6_0
    )

// Publish to Sonatype, https://www.scala-sbt.org/release/docs/Using-Sonatype.html
ThisBuild / credentials := List(
  Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
)
ThisBuild / description := "Package and run WAR files with sbt"
ThisBuild / developers := List(
  Developer(
    id = "earldouglas",
    name = "James Earl Douglas",
    email = "james@earldouglas.com",
    url = url("https://earldouglas.com/")
  )
)
ThisBuild / homepage := Some(
  url("https://github.com/earldouglas/sbt-war")
)
ThisBuild / licenses := List(
  "BSD New" -> url("https://opensource.org/licenses/BSD-3-Clause")
)
ThisBuild / organizationHomepage := Some(
  url("https://earldouglas.com/")
)
ThisBuild / organizationName := "James Earl Douglas"
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/earldouglas/sbt-war"),
    "scm:git@github.com:earldouglas/sbt-war.git"
  )
)
ThisBuild / versionScheme := Some("semver-spec")
