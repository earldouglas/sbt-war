package com.earldouglas.xwp

import sbt._, Keys._

object WarPlugin extends AutoPlugin {
  override def requires = WebappPlugin

  override lazy val projectSettings =
    Defaults.packageTaskSettings(packageBin, WebappPlugin.autoImport.webappPrepare) ++
    Seq(artifact := Artifact(moduleName.value, "war", "war"))
}
