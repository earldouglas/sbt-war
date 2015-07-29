package com.earldouglas.xwp

import sbt._, Keys._

object WarPlugin extends AutoPlugin {
  import Keys.{`package` => pkg}

  override def requires = WebappPlugin

  override lazy val projectSettings =
    Defaults.packageTaskSettings(pkg, WebappPlugin.autoImport.webappPrepare) ++
      Seq(artifact in pkg := Artifact(moduleName.value, "war", "war")) ++
      addArtifact(artifact in (Compile, pkg), pkg)

}
