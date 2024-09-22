package com.earldouglas.xwp

import com.earldouglas.sbt.war.WebappComponents
import com.earldouglas.sbt.war.WebappComponentsPlugin
import sbt.Def.settingKey
import sbt.Def.taskKey
import sbt.FilesInfo.exists
import sbt.FilesInfo.lastModified
import sbt.Keys._
import sbt._

import java.util.jar.Manifest

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

  override def requires = WebappComponentsPlugin

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      webappPrepare / sourceDirectory := (Compile / sourceDirectory).value / "webapp",
      webappPrepare / target := (Compile / target).value / "webapp",
      webappPrepareQuick / target := (Compile / target).value / "webapp-quick",
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

      val webappResourcesDir: File =
        (webappPrepare / sourceDirectory).value

      val webappTargetDir: File =
        webappTarget.value

      val resourceFiles: Set[File] =
        WebappComponents
          .getResources(webappResourcesDir)
          .filterNot(x => x._1.isDirectory())
          .map(_._1)
          .toSet

      cacheify(
        cacheName,
        { in =>
          for {
            f <- Some(in)
            r <- IO.relativizeFile(webappResourcesDir, f)
            t = IO.resolve(webappTargetDir, r)
          } yield t
        },
        resourceFiles,
        streams.value
      )

      webappTargetDir
    }

  private def webappPrepareQuickTask =
    Def.task {

      val webappTarget =
        _webappPrepare(
          webappPrepareQuick / target,
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
        _webappPrepare(webappPrepare / target, "webapp").value

      val webInfDir = webappTarget / "WEB-INF"
      val webappLibDir = webInfDir / "lib"

      val classpath: Seq[File] =
        (Runtime / fullClasspath).value
          .map(_.data)

      val webappClasses: Map[File, String] =
        WebappComponents.getClasses(classpath)

      // copy this project's classes directly to WEB-INF/classes
      def classesAsClasses(): Set[File] = {

        cacheify(
          "classes",
          { in =>
            webappClasses
              .find { case (src, dest) => src == in }
              .map { case (src, dest) => webInfDir / "classes" / dest }
          },
          webappClasses
            .filter { case (src, dest) => !src.isDirectory }
            .map { case (src, dest) => src }
            .toSet,
          taskStreams
        )
      }

      // copy this project's classes as a .jar file in WEB-INF/lib
      def classesAsJar(): Set[File] = {

        val jarFilename: String =
          (Compile / packageBin / packagedArtifact).value._2.getName()

        val outputJar = webappLibDir / jarFilename

        Compat.jar(
          sources = webappClasses,
          outputJar = outputJar,
          manifest = new Manifest
        )

        Set(outputJar)
      }

      if (webappWebInfClasses.value) {
        classesAsClasses()
      } else {
        classesAsJar()
      }

      // copy this project's library dependency .jar files to WEB-INF/lib
      cacheify(
        "lib-deps",
        { in => Some(webappTarget / "WEB-INF" / "lib" / in.getName()) },
        WebappComponents.getLib(classpath).keySet,
        taskStreams
      )

      webappPostProcess.value(webappTarget)

      (webappTarget ** "*") pair (Path.relativeTo(
        webappTarget
      ) | Path.flat)
    }

}
