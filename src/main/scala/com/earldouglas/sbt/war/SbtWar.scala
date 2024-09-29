package com.earldouglas.sbt.war

import sbt.AutoPlugin
import sbt.Plugins

/** The top-level plugin to be used by default. From the required
  * plugins, this brings in all of the webapp components mappings, .war
  * file packaging, and mechanisms for running both raw webapp
  * components and a packaged .war file.
  */
object SbtWar extends AutoPlugin {

  override val requires: Plugins =
    WarPackageRunnerPlugin && WebappComponentsRunnerPlugin
}
