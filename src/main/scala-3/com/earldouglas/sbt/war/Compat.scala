package com.earldouglas.sbt.war

import sbt.Def.Initialize
import sbt.Keys._
import sbt.Keys.{`package` => pkg}
import sbt._
import sbt.given

import java.io.{File => JavaFile}

import scala.language.implicitConversions

object Compat:

  val Compile_pkg_artifact = Compile / pkg / artifact
  val Compile_sourceDirectory = Compile / sourceDirectory
  val Compile_target = Compile / target
  val Global_onLoad = Global / onLoad
  val pkg_artifact = pkg / artifact

  val warContents: Initialize[Task[Map[String, HashedVirtualFileRef]]] =
    Def.task:
      val conv: FileConverter = fileConverter.value
      WebappComponentsPlugin
        .warContents
        .value
        .map:
          case (dst, src) => dst -> conv.toVirtualFile(src.toPath())

  def managedJars(config: Configuration): Initialize[Task[Seq[File]]] =
    Def.task:
      Classpaths
        .managedJars(config, classpathTypes.value, update.value, fileConverter.value)
        .map(_.data)
        .map(fileConverter.value.toPath(_))
        .map(_.toFile())
        .toList

  def toFile(file: TaskKey[HashedVirtualFileRef]): Initialize[Task[File]] =
    Def.task:
      fileConverter.value
        .toPath(file.value)
        .toFile()

  val classpathFiles: Initialize[Task[Seq[File]]] =
    Def.task:
      (Runtime / fullClasspath)
        .value
        .map(_.data)
        .map(fileConverter.value.toPath(_))
        .map(_.toFile())
