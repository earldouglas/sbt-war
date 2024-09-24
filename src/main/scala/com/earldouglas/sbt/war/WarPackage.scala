package com.earldouglas.sbt.war

import sbt.File

object WarPackage {

  def getWarContents(
      webappResources: Map[File, String],
      webappClasses: Map[File, String],
      webappLib: Map[File, String]
  ): Seq[(File, String)] =
    Seq(
      webappResources,
      webappClasses.map { case (k, v) => k -> s"classes/${v}" },
      webappLib.map { case (k, v) => k -> s"lib/${v}" }
    ).flatten
}
