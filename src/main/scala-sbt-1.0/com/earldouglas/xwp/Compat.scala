package com.earldouglas.xwp

import sbt._
import sbt.Keys._
import sbt.internal.io.Source
import sbt.util.CacheStoreFactory
import FileInfo.Style
import WebappPlugin.autoImport.webappPrepare

object Compat {

  type Process = scala.sys.process.Process

  def forkOptionsWithRunJVMOptions(options: Seq[String]) =
    ForkOptions().withRunJVMOptions(options.toVector)

  val watchSourceSetting = watchSources += new Source(
    (webappPrepare / sourceDirectory).value,
    AllPassFilter,
    NothingFilter
  )

  def cached(cacheBaseDirectory: File, inStyle: Style, outStyle: Style)(
      action: (ChangeReport[File], ChangeReport[File]) => Set[File]
  ): Set[File] => Set[File] =
    sbt.util.FileFunction.cached(
      CacheStoreFactory(cacheBaseDirectory),
      inStyle = inStyle,
      outStyle = outStyle
    )(action = action)

  def jar(
      sources: Traversable[(File, String)],
      outputJar: File,
      manifest: java.util.jar.Manifest
  ): Unit =
    io.IO.jar(
      sources = sources,
      outputJar = outputJar,
      manifest = manifest,
      time = None
    )
}
