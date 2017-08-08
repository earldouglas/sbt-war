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

  val watchSourceSetting = watchSources += new Source((sourceDirectory in webappPrepare).value, "*", AllPassFilter)

  def cached(cacheBaseDirectory: File, inStyle: Style, outStyle: Style)(action: (ChangeReport[File], ChangeReport[File]) => Set[File]): Set[File] => Set[File] = 
    sbt.util.FileFunction.cached(CacheStoreFactory(cacheBaseDirectory), inStyle = inStyle, outStyle = outStyle)(action = action)
}
