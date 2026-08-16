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
    case v =>
      throw new Exception(s"Unsupported Scala version ${v}")
  }
}

ThisBuild / scalaVersion := "2.12.21"
ThisBuild / crossScalaVersions := Seq("2.12.21", "3.8.4")
ThisBuild / javafmtFormatterCompatibleJavaVersion := 17

// Scalafix
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Testing
ThisBuild / libraryDependencies +=
  ("org.scalameta" %% "munit" % "1.3.5" % Test)
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

def warRunnerVersion(warSpec: String) =
  Def.setting {
    version.value
      .split("-")
      .toList match {
      case v :: Nil => s"""${v}_${warSpec}"""
      case v :: t   => s"""${v}_${warSpec}-${t.mkString("-")}"""
      case _        =>
        throw new Exception(s"Unparseable version: ${version.value}")
    }
  }

lazy val warRunner_6 =
  project
    .in(file("runners/6"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("6").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "8.5.68"
    )

lazy val warRunner_7 =
  project
    .in(file("runners/7"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("7").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "8.5.68",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "8.5.68"
    )

lazy val warRunner_8 =
  project
    .in(file("runners/8"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("8").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "9.0.113",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "9.0.113",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "9.0.113",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "9.0.113"
    )

lazy val warRunner_9 =
  project
    .in(file("runners/9"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("9").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "10.0.27",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "10.0.27",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "10.0.27",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "10.0.27"
    )

lazy val warRunner_10 =
  project
    .in(file("runners/10"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("10").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "10.1.57",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "10.1.57",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "10.1.57",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "10.1.57"
    )

lazy val warRunner_11 =
  project
    .in(file("runners/11"))
    .settings(
      name := "war-runner",
      version := warRunnerVersion("11").value,
      Compile / compile / javacOptions += "-g:lines",
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % "11.0.24",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % "11.0.24",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % "11.0.24",
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % "11.0.24"
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
            "2.0.0"
          case v =>
            throw new Exception(s"Unsupported Scala version ${v}")
        }
      },
      //
      // scripted-plugin
      scriptedBufferLog := false,
      watchSources ++= { (sourceDirectory.value ** "*").get },
      scriptedLaunchOpts += "-DtemplateDirectory=" + (sourceDirectory.value / "test" / "template"),
      scriptedBatchExecution := false,
      scriptedParallelInstances := 8,
      //
      // sbt-buildinfo
      buildInfoPackage := "com.earldouglas.sbt.war",
      buildInfoKeys := Seq[BuildInfoKey](version)
    )
    .aggregate(
      warRunner_6,
      warRunner_7,
      warRunner_8,
      warRunner_9,
      warRunner_10,
      warRunner_11
    )

// Publish to Sonatype, https://www.scala-sbt.org/release/docs/Using-Sonatype.html
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
ThisBuild / publishTo := {
  val centralSnapshots =
    "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/earldouglas/sbt-war"),
    "scm:git@github.com:earldouglas/sbt-war.git"
  )
)
ThisBuild / versionScheme := Some("semver-spec")
