package com.earldouglas.xwp

import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey
import sbt.Keys._

object WarPlugin extends AutoPlugin {
  import Keys.{`package` => pkg}

  override def requires = WebappPlugin

  object autoImport {
    val inheritJarManifest =
      settingKey[Boolean]("inherit .jar manifest")
  }

  import autoImport._

  private def manifestOptions =
    Def.task {
      val opt = (packageOptions in (Compile, packageBin)).value
      if (inheritJarManifest.value) {
        opt.filter {
          case x: Package.ManifestAttributes => true
          case _                             => false
        }
      } else {
        Seq.empty
      }
    }

  override lazy val projectSettings =
    Defaults.packageTaskSettings(
      pkg,
      WebappPlugin.autoImport.webappPrepare
    ) ++
      Seq(
        artifact in pkg := Artifact(moduleName.value, "war", "war")
      ) ++
      addArtifact(artifact in (Compile, pkg), pkg) ++
      Seq(
        inheritJarManifest := false,
        packageOptions in sbt.Keys.`package` ++= manifestOptions.value
      )

}
