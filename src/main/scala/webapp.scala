package com.earldouglas.xwp

import sbt._
import Keys._
import java.io.File
import java.util.jar.Manifest

trait WebappPlugin {

  lazy val webapp        = config("webapp").hide
  lazy val webappSrc     = SettingKey[File]("src")
  lazy val webappDest    = SettingKey[File]("dest")
  lazy val prepareWebapp = TaskKey[Seq[(sbt.File, String)]]("prepare")
  lazy val postProcess   = TaskKey[java.io.File => Unit]("post-process")
  lazy val webInfClasses = SettingKey[Boolean]("web-inf-classes")

  lazy val prepareWebappTask: Def.Initialize[Task[Seq[(File, String)]]] =
    (  postProcess in webapp
     , packagedArtifact in (Compile, packageBin)
     , mappings in (Compile, packageBin)
     , webInfClasses in webapp
     , webappSrc in webapp
     , webappDest in webapp
     , fullClasspath in Runtime
    ) map {
      case (  postProcess
            , (art, file)
            , mappings
            , webInfClasses
            , webappSrc
            , webappDest
            , fullClasspath
      ) =>

        IO.copyDirectory(webappSrc, webappDest)

        val webInfDir = webappDest / "WEB-INF"
        val webappLibDir = webInfDir / "lib"

        // copy this project's classes, either directly to WEB-INF/classes
        // or as a .jar file in WEB-INF/lib
        if (webInfClasses) {
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

        postProcess(webappDest)

        (webappDest ** "*") pair (relativeTo(webappDest) | flat)
      }

  lazy val webappSettings: Seq[Setting[_]] =
    Seq(
        webappSrc       := (sourceDirectory in Compile).value / "webapp"
      , webappDest      := (target in Compile).value / "webapp"
      , prepareWebapp   := prepareWebappTask.value
      , postProcess     := { _ => () }
      , webInfClasses   := false
      , watchSources  <++= (webappSrc in webapp) map { d => (d ** "*").get }
    )

}
