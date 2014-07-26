package com.earldouglas.xwp

import sbt._
import Keys._

trait WarPlugin { self: WebappPlugin =>

  lazy val packageWar = TaskKey[File]("package")

  lazy val warSettings: Seq[Setting[_]] =
    Defaults.packageTaskSettings(packageWar, prepareWebappTask) ++
    Seq(
        artifact in packageWar <<= moduleName(n => Artifact(n, "war", "war"))
      , Keys.`package` in Compile <<= packageWar
      , packageOptions in packageWar <<= packageOptions in (Compile, packageBin)
    ) ++
    webappSettings ++
    addArtifact(artifact in (Compile, packageWar), packageWar in Compile)

}
