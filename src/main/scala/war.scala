package com.earldouglas.xwp

import sbt._
import Keys._

trait WarPlugin { self: WebappPlugin =>
  import Keys.{`package` => pkg}

  lazy val warSettings: Seq[Setting[_]] =
    Defaults.packageTaskSettings(pkg, webappPrepareTask) ++
    Seq(artifact in pkg := Artifact(moduleName.value, "war", "war")) ++
    webappSettings

}
