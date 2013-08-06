package com.earldouglas.xsbtwebplugin

import sbt._
import Keys._
import PluginKeys._
import classpath.ClasspathUtilities._
import WarPlugin.warSettings0
  
object WebappPlugin extends Plugin {
  def auxCompileTask = (compile, crossTarget, classDirectory, excludeFilter) map {
    (_, ct, cd, filter) =>
    val auxCd = ct / "aux-classes"
    val classes = for {
      file <- cd.descendantsExcept("*", filter).get
      target = Path.rebase(cd, auxCd)(file).get
    } yield (file, target)
    val copied = IO.copy(classes)
    val toRemove = scala.collection.mutable.HashSet((auxCd ** "*").get.toSeq : _*) -- copied
    val (dirs, files) = toRemove.toList.partition(_.isDirectory)
    IO.delete(files)
    IO.deleteIfEmpty(dirs.toSet)
  }
  
  def webappSettings0(classpathConfig: Configuration):Seq[Setting[_]] = warSettings0(classpathConfig) ++ Seq(
    scanDirectories <<= crossTarget(ct => Seq(ct / "aux-classes")),
    auxCompile <<= auxCompileTask,
    scanInterval := 3,
    env := None,
    deployment <<= (webappResources, fullClasspath in classpathConfig, scanDirectories, scanInterval, env) map {
      (rs, cp, sd, si, env) =>
      Deployment(rs, cp.map(_.data), sd, si, env)
    }
  )
  def webappSettings0:Seq[Setting[_]] = webappSettings0(DefaultClasspathConf)
  def webappSettings(cc: Configuration):Seq[Setting[_]] = inConfig(DefaultConf)(webappSettings0(cc)) ++ WarPlugin.globalWarSettings
  def webappSettings:Seq[Setting[_]] = webappSettings(DefaultClasspathConf)
}
