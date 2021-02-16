package com.earldouglas.xwp

import sbt._
import sbt.Keys._
import sbt.FilesInfo.Style
import WebappPlugin.autoImport.webappPrepare

object Compat {

  type Process = sbt.Process

  def forkOptionsWithRunJVMOptions(options: Seq[String]) =
    ForkOptions(runJVMOptions = options)

  val watchSourceSetting = watchSources ++= ((sourceDirectory in webappPrepare).value ** "*").get

  def cached(cacheBaseDirectory: File, inStyle: Style, outStyle: Style)(action: (ChangeReport[File], ChangeReport[File]) => Set[File]): Set[File] => Set[File] = 
    FileFunction.cached(cacheBaseDirectory = cacheBaseDirectory)(inStyle = inStyle, outStyle = outStyle)(action = action)

  def jar(sources: Traversable[(File, String)], outputJar: File, manifest: java.util.jar.Manifest): Unit =
    IO.jar( sources = sources
          , outputJar = outputJar
          , manifest = manifest
          )
}
