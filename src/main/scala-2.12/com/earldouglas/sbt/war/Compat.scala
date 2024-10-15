package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt._

import java.io.{File => JavaFile}

object Compat {

  val Compile_pkg_artifact = Compile / pkg / artifact
  val Compile_sourceDirectory = Compile / sourceDirectory
  val Compile_target = Compile / target
  val Global_onLoad = Global / onLoad
  val pkg_artifact = pkg / artifact

  val warContents: Initialize[Task[Map[String, JavaFile]]] =
    WebappComponentsPlugin.warContents

  def managedJars(config: Configuration): Initialize[Task[Seq[File]]] =
    Def.task {
      Classpaths
        .managedJars(config, classpathTypes.value, update.value)
        .map(_.data)
        .toSeq
    }

  def toFile(file: TaskKey[File]): Initialize[Task[File]] =
    file

  val classpathFiles: Initialize[Task[Seq[File]]] =
    (Runtime / fullClasspath)
      .map(_.files)
}
