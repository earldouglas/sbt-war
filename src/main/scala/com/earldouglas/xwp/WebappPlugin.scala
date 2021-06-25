package com.earldouglas.xwp

import java.util.jar.Manifest

import sbt._
import sbt.Def.taskKey
import sbt.Def.settingKey
import sbt.Keys._
import sbt.FilesInfo.lastModified
import sbt.FilesInfo.exists
import sbt.FileFunction.cached

object WebappPlugin extends AutoPlugin {

  object autoImport {
    lazy val webappPrepare = taskKey[Seq[(File, String)]](
      "prepare webapp contents for packaging"
    )
    lazy val webappPrepareQuick = taskKey[Seq[(File, String)]](
      "prepare webapp contents for quickstart"
    )
    lazy val webappPostProcess = taskKey[File => Unit](
      "additional task after preparing the webapp"
    )
    lazy val webappWebInfClasses =
      settingKey[Boolean]("use WEB-INF/classes instead of WEB-INF/lib")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "webapp",
      target in webappPrepare := (target in Compile).value / "webapp",
      target in webappPrepareQuick := (target in Compile).value / "webapp-quick",
      webappPrepare := webappPrepareTask.value,
      webappPrepareQuick := webappPrepareQuickTask.value,
      webappPostProcess := { _ => () },
      webappWebInfClasses := false,
      Compat.watchSourceSetting
    )

  private def cacheify(
      name: String,
      dest: File => Option[File],
      in: Set[File],
      streams: TaskStreams
  ): Set[File] =
    Compat
      .cached(
        streams.cacheDirectory / "xsbt-web-plugin" / name,
        lastModified,
        exists
      )({ (inChanges, outChanges) =>
        // toss out removed files
        for {
          removed <- inChanges.removed
          toRemove <- dest(removed)
        } yield IO.delete(toRemove)

        // new files
        val newFiles =
          for {
            in <- inChanges.added -- inChanges.removed
            out <- dest(in)
            _ = IO.copyFile(in, out)
          } yield out

        // modified files
        val modifiedFiles =
          for {
            in <- inChanges.modified -- inChanges.removed
            out <- dest(in)
            _ = IO.copyFile(in, out)
          } yield out

        // missing files
        val missingFiles =
          for {
            in <- inChanges.checked -- inChanges.removed
            out <- dest(in).toSet & outChanges.modified
            _ = IO.copyFile(in, out)
          } yield out

        // all files
        newFiles ++ modifiedFiles ++ missingFiles
      })
      .apply(in)

  private def _webappPrepare(
      webappTarget: SettingKey[File],
      cacheName: String
  ) =
    Def.task {

      val webappSrcDir = (sourceDirectory in webappPrepare).value

      cacheify(
        cacheName,
        { in =>
          for {
            f <- Some(in)
            if !f.isDirectory
            r <- IO.relativizeFile(webappSrcDir, f)
          } yield IO.resolve(webappTarget.value, r)
        },
        (webappSrcDir ** "*").get.toSet,
        streams.value
      )

      webappTarget.value
    }

  private def webappPrepareQuickTask =
    Def.task {

      val webappTarget =
        _webappPrepare(
          target in webappPrepareQuick,
          "webapp-quick"
        ).value

      webappPostProcess.value(webappTarget)

      (webappTarget ** "*") pair (Path.relativeTo(
        webappTarget
      ) | Path.flat)
    }

  private def webappPrepareTask =
    Def.task {

      val taskStreams = streams.value

      val webappTarget =
        _webappPrepare(target in webappPrepare, "webapp").value

      val m = (mappings in (Compile, packageBin)).value
      val p = (packagedArtifact in (Compile, packageBin)).value._2

      val webInfDir = webappTarget / "WEB-INF"
      val webappLibDir = webInfDir / "lib"

      if (webappWebInfClasses.value) {
        // copy this project's classes directly to WEB-INF/classes
        cacheify(
          "classes",
          { in =>
            m find {
              case (src, dest) => src == in
            } map {
              case (src, dest) =>
                webInfDir / "classes" / dest
            }
          },
          (m filter {
            case (src, dest) => !src.isDirectory
          } map {
            case (src, dest) =>
              src
          }).toSet,
          taskStreams
        )
      } else {
        // copy this project's classes as a .jar file in WEB-INF/lib
        cacheify(
          "lib-art",
          { in => Some(webappLibDir / in.getName) },
          Set(p),
          taskStreams
        )
      }

      val classpath = (fullClasspath in Runtime).value

      // create .jar files for depended-on projects in WEB-INF/lib
      for {
        cpItem <- classpath.toList
        dir = cpItem.data
        if dir.isDirectory
        artEntry <- cpItem.metadata.entries find { e =>
          e.key.label == "artifact"
        }
        cpArt = artEntry.value.asInstanceOf[Artifact]
        artifact = (packagedArtifact in (Compile, packageBin)).value._1
        if cpArt != artifact
        files = (dir ** "*").get flatMap { file =>
          if (!file.isDirectory)
            IO.relativize(dir, file) map { p => (file, p) }
          else
            None
        }
        jarFile = cpArt.name + ".jar"
        _ = Compat.jar(
          sources = files,
          outputJar = webappLibDir / jarFile,
          manifest = new Manifest
        )
      } yield ()

      // copy this project's library dependency .jar files to WEB-INF/lib
      cacheify(
        "lib-deps",
        { in => Some(webappTarget / "WEB-INF" / "lib" / in.getName) },
        classpath.map(_.data).toSet filter { in =>
          !in.isDirectory && in.getName.endsWith(".jar")
        },
        taskStreams
      )

      webappPostProcess.value(webappTarget)

      (webappTarget ** "*") pair (Path.relativeTo(
        webappTarget
      ) | Path.flat)
    }

}
