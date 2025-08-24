package com.earldouglas.sbt.war

import sbt.*
import sbt.Def.Initialize
import sbt.Keys.*

import java.io.File as JavaFile

object Compat:

  type PackageFile = HashedVirtualFileRef

  val toPackageFile: Initialize[Task[JavaFile => PackageFile]] =
    Def.task:
      (x: JavaFile) =>
        val conv: FileConverter = fileConverter.value
        conv.toVirtualFile(x.toPath())

  def fromPackageFile(file: TaskKey[HashedVirtualFileRef]): Initialize[Task[File]] =
    Def.task:
      fileConverter.value
        .toPath(file.value)
        .toFile()

  val managedJars: Initialize[Task[Seq[File]]] =
    Def.task:
      Classpaths
        .managedJars(
          SbtWar.autoImport.War,
          classpathTypes.value,
          update.value,
          fileConverter.value
        )
        .map(_.data)
        .map(fileConverter.value.toPath(_))
        .map(_.toFile())
        .toSeq

  def classpathFiles(c: Configuration): Initialize[Task[Seq[File]]] =
    Def.task:
      (c / fullClasspath)
        .value
        .map(_.data)
        .map(fileConverter.value.toPath(_))
        .map(_.toFile())
