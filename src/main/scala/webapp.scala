package com.earldouglas.xwp

import sbt._
import Keys._

trait WebappPlugin { self: Plugin =>

  lazy val webapp        = config("webapp").hide
  lazy val webappSrc     = TaskKey[File]("src")
  lazy val webappDest    = TaskKey[File]("dest")
  lazy val prepareWebapp = TaskKey[Seq[(sbt.File, String)]]("prepare")
  lazy val postProcess   = TaskKey[java.io.File => Unit]("post-process")
  lazy val webInfClasses = TaskKey[Boolean]("web-inf-classes")
  lazy val webappDeps    = TaskKey[Seq[File]]("webapp-deps")

  implicit class WebappProject(project: Project) {
    import sbt.Keys._
    def webappDependsOn(deps: Project*): Project =
      project.settings(
        webappDeps in webapp <<=
         (deps map { p => packagedArtifact in Compile in packageBin in p
          }).join map { xs => xs map { _._2 } }
        ) dependsOn (deps map { x => x: ClasspathDep[ProjectReference] }:_*)
  }

  lazy val prepareWebappTask: Def.Initialize[Task[Seq[(File, String)]]] =
    (  postProcess in webapp
     , packagedArtifact in (Compile, packageBin)
     , webappDeps in webapp
     , mappings in (Compile, packageBin)
     , webInfClasses in webapp
     , webappSrc in webapp
     , webappDest in webapp
     , fullClasspath in Runtime
    ) map {
      case (  postProcess
            , (_, file)
            , webappDeps
            , mappings
            , webInfClasses
            , webappSrc
            , webappDest
            , fullClasspath
      ) =>

        IO.copyDirectory(webappSrc, webappDest)

        val webInfDir = webappDest / "WEB-INF"
        val webappLibDir = webInfDir / "lib"

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

        webappDeps foreach { file =>
            IO.copyFile(file, webappLibDir / file.getName)
        }

        for {
          file <- fullClasspath.toList map { _.data }
          if !file.isDirectory
          name = file.getName
          if name.endsWith(".jar")
        } yield IO.copyFile(file, webappLibDir / name)

        postProcess(webappDest)

        (webappDest ** "*") pair (relativeTo(webappDest) | flat)
      }

  lazy val webappSettings: Seq[Setting[_]] =
    Seq(
        webappSrc      <<= (sourceDirectory in Compile) map { _ / "webapp" }
      , webappDest     <<= (target in Compile) map { _ / "webapp" }
      , prepareWebapp  <<= prepareWebappTask
      , postProcess     := { _ => () }
      , webInfClasses   := false
      , watchSources <++= (webappSrc in webapp) map { d => (d ** "*").get }
      , webappDeps     := Nil
    )

}
