package com.earldouglas.xwp

import sbt._

object XwpPlugin extends Plugin
                    with WebappPlugin
                    with WarPlugin
                    with ContainerPlugin {

  lazy val xwpSettings = warSettings ++ containerSettings

}
