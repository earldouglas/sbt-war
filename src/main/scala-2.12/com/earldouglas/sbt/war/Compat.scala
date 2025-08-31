package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys._
import sbt._

import java.io.{File => JavaFile}

object Compat {

  type PackageFile = JavaFile

  val toPackageFile: Initialize[Task[JavaFile => PackageFile]] =
    Def.task(identity)

  def fromPackageFile(file: TaskKey[File]): Initialize[Task[File]] =
    file

  val managedJars: Initialize[Task[Seq[File]]] =
    Def.task {
      Classpaths
        .managedJars(
          SbtWar.autoImport.War,
          classpathTypes.value,
          update.value
        )
        .map(_.data)
        .toSeq
    }

  def classpathFiles(c: Configuration): Initialize[Task[Seq[File]]] =
    Def.task {
      (c / fullClasspath).value
        .map(_.data)
    }
}
