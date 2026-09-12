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
  ("org.scalameta" %% "munit" % "1.3.6" % Test)
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

def warRunner(
    servletSpec: String,
    tomcatEmbedVersion: String
): Project =
  Project(
    id = s"warRunner_${servletSpec.replaceAll("""\.""", "_")}",
    base = file(s"runners/${servletSpec}")
  )
    .settings(
      name := "war-runner",
      version := warRunnerVersion(servletSpec).value,
      Compile / compile / javacOptions += "-g:lines",
      Compile / sourceGenerators += Def
        .task(
          ((baseDirectory.value / ".." / ".." / "runner" / "src" / "main" / "java") ** "*").get
            .filter(_.isFile())
        )
        .taskValue,
      Test / sourceGenerators += Def
        .task(
          ((baseDirectory.value / ".." / ".." / "runner" / "src" / "test" / "scala") ** "*").get
            .filter(_.isFile())
        )
        .taskValue,
      Test / resourceGenerators += Def
        .task(
          ((baseDirectory.value / ".." / ".." / "runner" / "src" / "test" / "resources") ** "*").get
            .filter(_.isFile())
        )
        .taskValue,
      crossPaths := false, // exclude Scala suffix from artifact names
      autoScalaLibrary := false, // exclude scala-library from dependencies
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-core" % tomcatEmbedVersion,
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-el" % tomcatEmbedVersion,
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-jasper" % tomcatEmbedVersion,
      libraryDependencies += "org.apache.tomcat.embed" % "tomcat-embed-websocket" % tomcatEmbedVersion
    )

lazy val warRunner_3_0 =
  warRunner("3.0", "8.5.68") // TODO use 7.0.x instead of 8.5.x

lazy val warRunner_3_1 = warRunner("3.1", "8.5.68")

lazy val warRunner_4_0 = warRunner("4.0", "9.0.120")

lazy val warRunner_5_0 = warRunner("5.0", "10.0.27")

lazy val warRunner_6_0 = warRunner("6.0", "10.1.57")

lazy val warRunner_6_1 = warRunner("6.1", "11.0.25")

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
      warRunner_6_0,
      warRunner_6_1
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
