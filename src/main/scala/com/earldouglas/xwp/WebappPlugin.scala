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

    def cacheify(name: String, dest: File => Option[File], in: Set[File]): Set[File] =
      cached(streams.value.cacheDirectory / "xsbt-web-plugin" / name)(lastModified, exists)({
        (inChanges, outChanges) =>
          // toss out removed files
          for {
            removed  <- inChanges.removed
            toRemove <- dest(removed)
          } yield IO.delete(toRemove)

          // apply and report changes
          for {
            in  <- inChanges.added ++ inChanges.modified -- inChanges.removed
            out <- dest(in)
            _    = IO.copyFile(in, out)
          } yield out
      }).apply(in)

    val webappSrcDir = (sourceDirectory in webappPrepare).value
    val webappTarget = (target in webappPrepare).value
    val classpath = (fullClasspath in Runtime).value
    val webInfDir = webappTarget / "WEB-INF"
    val webappLibDir = webInfDir / "lib"

    cacheify(
      "webapp",
      { in =>
        for {
          f <- Some(in)
          if !f.isDirectory
          r <- IO.relativizeFile(webappSrcDir, f)
        } yield IO.resolve(webappTarget, r)
      },
      (webappSrcDir ** "*").get.toSet
    )

    if (webappWebInfClasses.value) {
      // copy this project's classes directly to WEB-INF/classes
      cacheify(
        "classes",
        { in =>
          (mappings in (Compile, packageBin)).value find {
            case (src, dest) => src == in
          } map { case (src, dest) =>
            webInfDir / "classes" / dest
          }
        },
        ((mappings in (Compile, packageBin)).value filter {
          case (src, dest) => !src.isDirectory
        } map { case (src, dest) =>
          src
        }).toSet
      )
    } else {
      // copy this project's classes as a .jar file in WEB-INF/lib
      cacheify(
        "lib-art",
        { in => Some(webappLibDir / in.getName) },
        Set((packagedArtifact in (Compile, packageBin)).value._2)
      )
    }

    // create .jar files for depended-on projects in WEB-INF/lib
    for {
      cpItem    <- classpath.toList
      dir        = cpItem.data
      if dir.isDirectory
      artEntry  <- cpItem.metadata.entries find { e => e.key.label == "artifact" }
      cpArt      = artEntry.value.asInstanceOf[Artifact]
      artifact   = (packagedArtifact in (Compile, packageBin)).value._1
      if cpArt  != artifact
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
      { in => Some(webappTarget / "WEB-INF" / "lib" / in.getName) },
      classpath.map(_.data).toSet filter { in =>
        !in.isDirectory && in.getName.endsWith(".jar")
      }
    )

    webappPostProcess.value(webappTarget)

    (webappTarget ** "*") pair (relativeTo(webappTarget) | flat)
  }

}
