package com.earldouglas.xwp

import java.util.jar.Manifest

import sbt._
import sbt.Keys._
import sbt.FilesInfo.lastModified
import sbt.FilesInfo.exists
import sbt.FileFunction.cached

object WebappPlugin extends AutoPlugin {

  object autoImport {
    lazy val webappPrepare       = taskKey[Seq[(File, String)]]("prepare webapp contents for packaging")
    lazy val webappPostProcess   = taskKey[File => Unit]("additional task after preparing the webapp")
    lazy val webappWebInfClasses = settingKey[Boolean]("use WEB-INF/classes instead of WEB-INF/lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override def projectSettings: Seq[Setting[_]] =
    Seq(
        sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "webapp"
      , target in webappPrepare          := (target in Compile).value / "webapp"
      , webappPrepare                    := webappPrepareTask.value
      , webappPostProcess                := { _ => () }
      , webappWebInfClasses              := false
      , watchSources                    ++= ((sourceDirectory in webappPrepare).value ** "*").get
    )

  private def webappPrepareTask = Def.task {

    def cacheify(name: String, dest: File => File, in: Set[File]): Set[File] =
      cached(streams.value.cacheDirectory / "xsbt-web-plugin" / name)(lastModified, exists)({
        (inChanges, outChanges) =>
          // toss out removed files
          inChanges.removed foreach { in => IO.delete(dest(in)) }

          // apply and report changes
          (inChanges.added ++ inChanges.modified) map { in =>
            val out = dest(in)
            IO.copyFile(in, out)
            out
          }
      }).apply(in)

    val (art, file) = (packagedArtifact in (Compile, packageBin)).value
    val webappSrcDir = (sourceDirectory in webappPrepare).value
    val webappTarget = (target in webappPrepare).value
    val classpath = (fullClasspath in Runtime).value

    IO.copyDirectory(webappSrcDir, webappTarget)

    val webInfDir = webappTarget / "WEB-INF"
    val webappLibDir = webInfDir / "lib"

    // copy this project's classes, either directly to WEB-INF/classes
    // or as a .jar file in WEB-INF/lib
    if (webappWebInfClasses.value) {
      (mappings in (Compile, packageBin)).value foreach {
        case (src, dest) =>
          if (!src.isDirectory) {
            val destFile = webInfDir / "classes" / dest
            IO.copyFile(src, destFile)
          }
      }
    } else {
      IO.copyFile(file, webappLibDir / file.getName)
    }

    // create .jar files for depended-on projects in WEB-INF/lib
    for {
      cpItem    <- classpath.toList
      dir        = cpItem.data
      if dir.isDirectory
      artEntry  <- cpItem.metadata.entries find { e => e.key.label == "artifact" }
      cpArt      = artEntry.value.asInstanceOf[Artifact]
      if cpArt  != art
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
    cacheify(
      "lib-deps",
      { in: File => webappTarget / "WEB-INF" / "lib" / in.getName },
      classpath.map(_.data).toSet filter { in =>
        !in.isDirectory && in.getName.endsWith(".jar")
      }
    )

    webappPostProcess.value(webappTarget)

    (webappTarget ** "*") pair (relativeTo(webappTarget) | flat)
  }

}
