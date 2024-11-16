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

lazy val warRunner_3_0 =
  project
    .in(file("runners/3.0"))
    .settings(
      name := "war-runner",
      version := version.value + "_3.0",
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "17",
          "-target",
          "17",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.22.v20160922"
    )

lazy val warRunner_3_1 =
  project
    .in(file("runners/3.1"))
    .settings(
      name := "war-runner",
      version := version.value + "_3.1",
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "17",
          "-target",
          "17",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "8.5.68.1",
      libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.4.56.v20240826"
    )

lazy val warRunner_4_0 =
  project
    .in(file("runners/4.0"))
    .settings(
      name := "war-runner",
      version := version.value + "_4.0",
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "17",
          "-target",
          "17",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "9.0.96.0",
      libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "10.0.24"
    )

lazy val warRunner_5_0 =
  project
    .in(file("runners/5.0"))
    .settings(
      name := "war-runner",
      version := version.value + "_5.0",
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "17",
          "-target",
          "17",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "10.1.31.0",
      libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "11.0.24"
    )

lazy val warRunner_6_0 =
  project
    .in(file("runners/6.0"))
    .settings(
      name := "war-runner",
      version := version.value + "_6.0",
      Compile / compile / javacOptions ++=
        Seq(
          "-source",
          "17",
          "-target",
          "17",
          "-g:lines"
        ),
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "com.heroku" % "webapp-runner" % "10.1.31.0",
      libraryDependencies += "org.eclipse.jetty.ee10" % "jetty-ee10-webapp" % "12.0.15"
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
      scriptedLaunchOpts += "-DtemplateDirectory=" + (sourceDirectory.value / "template"),
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
      warRunner_5_0,
      warRunner_6_0
    )

// Publish to Sonatype, https://www.scala-sbt.org/release/docs/Using-Sonatype.html
ThisBuild / credentials := List(
  Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
)
ThisBuild / description := "Package and run .war files with sbt"
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
ThisBuild / publishTo := Some(
  "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/earldouglas/sbt-war"),
    "scm:git@github.com:earldouglas/sbt-war.git"
  )
)
ThisBuild / versionScheme := Some("semver-spec")
