package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File
import java.util.jar.Manifest

trait WebappPlugin {

  lazy val webappSrc     = SettingKey[File]("webapp-src")
  lazy val webappDest    = SettingKey[File]("webapp-dest")
  lazy val webappPrepare = TaskKey[Seq[(sbt.File, String)]]("webapp-prepare")
  lazy val webappPostProcess   = TaskKey[java.io.File => Unit]("webapp-post-process")
  lazy val webappWebInfClasses = TaskKey[Boolean]("webapp-web-inf-classes")

  lazy val webappPrepareTask: Def.Initialize[Task[Seq[(File, String)]]] =
    (  webappPostProcess
     , packagedArtifact in (Compile, packageBin)
     , mappings in (Compile, packageBin)
     , webappWebInfClasses
     , webappSrc
     , webappDest
     , fullClasspath in Runtime
    ) map {
      case (  webappPostProcess
            , (art, file)
            , mappings
            , webappWebInfClasses
            , webappSrc
            , webappDest
            , fullClasspath
      ) =>

        IO.copyDirectory(webappSrc, webappDest)

        val webInfDir = webappDest / "WEB-INF"
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
          files      = (dir ** "*").getPaths flatMap { p =>
                         val file = new File(p)
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

        webappPostProcess(webappDest)

        (webappDest ** "*") pair (relativeTo(webappDest) | flat)
      }

  lazy val webappSettings: Seq[Setting[_]] =
    Seq(
        webappSrc       := (sourceDirectory in Compile).value / "webapp"
      , webappDest      := (target in Compile).value / "webapp"
      , webappPrepare   := webappPrepareTask.value
      , webappPostProcess     := { _ => () }
      , webappWebInfClasses   := false
      , watchSources  <++= webappSrc map { d => (d ** "*").get }
    )

}
