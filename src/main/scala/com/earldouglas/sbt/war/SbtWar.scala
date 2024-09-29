package com.earldouglas.sbt.war

import sbt.AutoPlugin
import sbt.Plugins

object SbtWar extends AutoPlugin {

  override val requires: Plugins =
    WarPackageRunnerPlugin && WebappComponentsRunnerPlugin
}
