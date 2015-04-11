package com.earldouglas.xwp

import sbt._
import Keys._

trait WarPlugin { self: WebappPlugin =>

  lazy val webappPackageWar = TaskKey[File]("webapp-package-war")

  lazy val warSettings: Seq[Setting[_]] =
    Defaults.packageTaskSettings(webappPackageWar, webappPrepareTask) ++
    Seq(
        artifact in webappPackageWar <<= moduleName(n => Artifact(n, "war", "war"))
      , Keys.`package` in Compile <<= webappPackageWar
      , packageOptions in webappPackageWar <<= packageOptions in (Compile, packageBin)
    ) ++
    webappSettings ++
    addArtifact(artifact in (Compile, webappPackageWar), webappPackageWar in Compile)

}
