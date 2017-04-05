package com.earldouglas.xwp

import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey
import sbt.Keys._
import sbt.Keys.{`package` => pkg}

object HerokuDeploy extends AutoPlugin {

  lazy val Deploy = config("deploy").hide

  object autoImport {
    lazy val herokuOptions    = settingKey[Seq[String]]("Extra options for heroku-deploy")
    lazy val herokuAppName    = settingKey[String]("Heroku App name")
    lazy val herokuWarFile    = taskKey[File](".war file to deploy to Heroku")
    lazy val herokuDeploy     = taskKey[Unit]("Deploy .war file to Heroku")
    lazy val herokuDeployLib  = settingKey[ModuleID]("heroku-deploy library")
    lazy val herokuDeployMain = settingKey[String]("heroku-deploy main class")
  }

  import autoImport._

  override val projectConfigurations = Seq(Deploy)

  def deploy(herokuOptions: Seq[String],
             herokuAppName: String,
             herokuWarFile: java.io.File,
             classpathTypes: Set[String],
             update: UpdateReport,
             herokuDeployMain: String): Unit = {
    val options = ForkOptions(
      runJVMOptions =
        herokuOptions ++ Seq(
          "-Dheroku.appName=" + herokuAppName,
          "-Dheroku.warFile=" + herokuWarFile.getPath
        )
    )
    val libs: Seq[File] =
      Classpaths.managedJars(Deploy, classpathTypes, update).map(_.data)

    val cp: String = libs map (_.getPath) mkString java.io.File.pathSeparator

    Fork.java(options, Seq("-cp", cp, herokuDeployMain))
  }

  override def projectSettings = Seq(
    herokuOptions       := Seq("-Xmx1g"),
    herokuWarFile       := (packagedArtifact in (Compile, pkg), pkg)._2.value,
    herokuDeployLib     := "com.heroku.sdk" % "heroku-deploy" % "1.1.3",
    herokuDeployMain    := "com.heroku.sdk.deploy.DeployWar",
    libraryDependencies += herokuDeployLib.value % Deploy,
    herokuDeploy        := deploy(herokuOptions.value,
                                  herokuAppName.value,
                                  herokuWarFile.value,
                                  (classpathTypes in Deploy).value,
                                  (update in Deploy).value,
                                  herokuDeployMain.value)
  )

} 
