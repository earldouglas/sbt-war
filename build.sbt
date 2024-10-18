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

lazy val warRunner =
  project
    .in(file("runner"))
    .settings(
      //
      name := "war-runner",
      //
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
      //
      // webapp-runner
      libraryDependencies += "com.heroku" % "webapp-runner" % "10.1.28.0"
    )

lazy val root =
  project
    .in(file("."))
    .enablePlugins(SbtPlugin)
    .enablePlugins(BuildInfoPlugin)
    .settings(
      //
      name := "sbt-war",
      //
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
      //
    )
    .dependsOn(warRunner)
    .aggregate(warRunner)

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
