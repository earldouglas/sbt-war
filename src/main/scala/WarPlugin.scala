package com.earldouglas.xwp

import sbt._
import Keys._

object WarPlugin extends AutoPlugin {
  import Keys.{`package` => pkg}

  override def requires = WebappPlugin

  override def trigger = allRequirements

  override lazy val projectSettings =
    Defaults.packageTaskSettings(pkg, WebappPlugin.autoImport.webappPrepare) ++
      Seq(artifact in pkg := Artifact(moduleName.value, "war", "war"))
}
