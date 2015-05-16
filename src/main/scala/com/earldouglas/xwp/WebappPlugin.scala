package com.earldouglas.xwp

import java.io.File
import java.util.jar.Manifest

import sbt._, Keys._

object WebappPlugin extends AutoPlugin {

  object autoImport {
    lazy val webappPrepare = taskKey[Seq[(sbt.File, String)]]("prepare webapp contents for packaging")
    lazy val webappPostProcess = taskKey[java.io.File => Unit]("additional task after preparing the webapp")
    lazy val webappWebInfClasses = taskKey[Boolean]("use WEB-INF/classes instead of WEB-INF/lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[_]] =
    Seq(
        sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "webapp"
      , target in webappPrepare          := (target in Compile).value / "webapp"
      , webappPrepare                    := webappPrepareTask.value
      , webappPostProcess                := { _ => () }
      , webappWebInfClasses              := false
      , watchSources                    ++= ((sourceDirectory in webappPrepare).value ** "*").get
    )

  private def webappPrepareTask: Def.Initialize[Task[Seq[(File, String)]]] =
    ( webappPostProcess
    , packagedArtifact in (Compile, packageBin)
    , mappings         in (Compile, packageBin)
    , webappWebInfClasses
    , sourceDirectory in webappPrepare
    , target          in webappPrepare
    , fullClasspath   in Runtime
    ) map {
      case ( webappPostProcess
           , (art, file)
           , mappings
           , webappWebInfClasses
           , webappSrcDir
           , webappTarget
           , fullClasspath
           ) =>

        IO.copyDirectory(webappSrcDir, webappTarget)

        val webInfDir = webappTarget / "WEB-INF"
        val webappLibDir = webInfDir / "lib"

        // copy this project's classes, either directly to WEB-INF/classes
        // or as a .jar file in WEB-INF/lib
        if (webappWebInfClasses) {
          mappings foreach {
            case (src, name) =>
              if (!src.isDirectory) {
                val dest =  webInfDir / "classes" / name
                IO.copyFile(src, dest)
              }
          }
        } else {
          IO.copyFile(file, webappLibDir / file.getName)
        }

        // create .jar files for depended-on projects in WEB-INF/lib
        for {
          cpItem    <- fullClasspath.toList
          dir        = cpItem.data
          if dir.isDirectory
          artEntry  <- cpItem.metadata.entries find { e => e.key.label == "artifact" }
          cpArt      = artEntry.value.asInstanceOf[Artifact]
          if cpArt != art//(cpItem.metadata.entries exists { _.value == art })
          files      = (dir ** "*").get flatMap { file =>
            if (!file.isDirectory)
              IO.relativize(dir, file) map { p => (file, p) }
            else
              None
          }
          jarFile    = cpArt.name + ".jar"
          _          = IO.jar(files, webappLibDir / jarFile, new Manifest)
        } yield ()

        // copy this project's library dependency .jar files to WEB-INF/lib
        for {
          cpItem <- fullClasspath.toList
          file    = cpItem.data
          if !file.isDirectory
          name    = file.getName
          if name.endsWith(".jar")
        } yield IO.copyFile(file, webappLibDir / name)

        webappPostProcess(webappTarget)

        (webappTarget ** "*") pair (relativeTo(webappTarget) | flat)
    }

}
