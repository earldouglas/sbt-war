// General
ThisBuild / organization := "com.earldouglas"
ThisBuild / scalacOptions ++=
  Seq(
    "-feature",
    "-deprecation"
  )
ThisBuild / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      Seq(
        "-Xsource:3",
        "-Ywarn-unused-import",
        s"-P:semanticdb:sourceroot:${baseDirectory.value}"
      )
    case Some((3, _)) =>
      Seq(
        "-Wunused:imports"
      )
  }
}

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / crossScalaVersions := Seq("2.13.18", "3.7.4")

// Scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Testing
ThisBuild / libraryDependencies +=
  ("org.scalameta" %% "munit" % "1.2.1" % Test)
    .exclude("org.scala-lang", "scala3_library_3")
    .exclude("org.scala-lang", "scala-library")
ThisBuild / libraryDependencies += {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      "org.scala-lang" % "scala-library" % scalaVersion.value % Test
    case Some((3, _)) =>
      "org.scala-lang" % "scala3-library_3" % scalaVersion.value % Test
    case v =>
      throw new Exception(s"Unsupported Scala version: ${v}")
  }
}
ThisBuild / Test / fork := true

def warRunnerVersion(servletSpec: String) =
  Def.setting {
    version.value
      .split("-")
      .toList match {
      case v :: Nil => s"""${v}_${servletSpec}"""
      case v :: t   => s"""${v}_${servletSpec}-${t.mkString("-")}"""
      case _        =>
        throw new Exception(s"Unparseable version: ${version.value}")
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
      libraryDependencies += "com.heroku" % "webapp-runner" % "9.0.112.0"
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
      libraryDependencies += "com.heroku" % "webapp-runner" % "10.1.49.0"
    )

lazy val sbtWar =
  project
    .in(file("."))
    .enablePlugins(SbtPlugin)
    .enablePlugins(BuildInfoPlugin)
    .settings(
      name := "sbt-war",
      sbtPlugin := true,
      pluginCrossBuild / sbtVersion := {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 12)) =>
            (pluginCrossBuild / sbtVersion).value
          case Some((3, _)) =>
            "2.0.0-RC7"
        }
      },
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
ThisBuild / sbtPluginPublishLegacyMavenStyle := false
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := Sonatype.sonatypeCentralHost
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/earldouglas/sbt-war"),
    "scm:git@github.com:earldouglas/sbt-war.git"
  )
)
ThisBuild / versionScheme := Some("semver-spec")
