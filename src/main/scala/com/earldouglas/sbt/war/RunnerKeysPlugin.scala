package com.earldouglas.sbt.war
import sbt.Def.settingKey
import sbt.Def.taskKey
import sbt._

object RunnerKeysPlugin extends AutoPlugin {

  object autoImport {
    lazy val port = settingKey[Int]("container port")
    lazy val start = taskKey[Unit]("start container")
    lazy val join = taskKey[Unit]("join container")
    lazy val stop = taskKey[Unit]("stop container")
  }
}
