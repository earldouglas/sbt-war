package com.github.siasia

import sbt.{`package` => _, _}
import Project.Initialize
import Keys._
import PluginKeys._
import _root_.sbt.Defaults.{packageTasks, packageBinTask}
import _root_.sbt.Classpaths.analyzed

object WarPlugin extends Plugin {
	 private def copyFlat(sources: Iterable[File], destinationDirectory: File): Set[File] = {
    val map = sources.map(source => (source.asFile, destinationDirectory / source.getName))
    IO.copy(map)
  }
	
	def packageWarTask: Initialize[Task[Seq[(File, String)]]] =
		(webappResources, target, fullClasspath, excludeFilter, streams) map {
			(webappResources, target, fullClasspath, filter, s) =>
			val classpath = fullClasspath.map(_.data)
			val warPath = target / "webapp"
			val log = s.log.asInstanceOf[AbstractLogger]    
			import _root_.sbt.classpath.ClasspathUtilities
			val webInfPath = warPath / "WEB-INF"
			val webLibDirectory = webInfPath / "lib"
			val classesTargetDirectory = webInfPath / "classes"

			val (libs, directories) = classpath.toList.partition(ClasspathUtilities.isArchive)
			val wcToCopy = for {
				dir <- webappResources
				file <- dir.descendentsExcept("*", filter).get
				val target = Path.rebase(dir, warPath)(file).get
			} yield (file, target)
			val classesAndResources = for {
				dir <- directories
				file <- dir.descendentsExcept("*", filter).get
				val target = Path.rebase(dir, classesTargetDirectory)(file).get
			} yield (file, target)
			if(log.atLevel(Level.Debug))
				directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

			val copiedWebapp = IO.copy(wcToCopy)
			val copiedClasses = IO.copy(classesAndResources)
			val copiedLibs = copyFlat(libs, webLibDirectory)
			val toRemove = scala.collection.mutable.HashSet((warPath ** "*").get.toSeq : _*)
			toRemove --= copiedWebapp
			toRemove --= copiedClasses
			toRemove --= copiedLibs
			val (dirs, files) = toRemove.toList.partition(_.isDirectory)
			if(log.atLevel(Level.Debug))
				files.foreach(r => log.debug("Pruning file " + r))
			IO.delete(files)
			IO.deleteIfEmpty(dirs.toSet)
			(warPath).descendentsExcept("*", filter) x (relativeTo(warPath)|flat)
		}
	def exportProductsTask: Initialize[Task[Classpath]] =
		(products.task, packageJar.task, exportJars, compile) flatMap { (psTask, pkgTask, useJars, analysis) =>
			(if(useJars) Seq(pkgTask).join else psTask) map { _ map { f => analyzed(f, analysis) } }
		}
	def warSettings0 = packageTasks(packageBin, packageWarTask) ++ Seq(
			webappResources <<= sourceDirectory(sd => Seq(sd / "webapp")),
			configuration in packageBin := DefaultConf,
			artifact in packageBin <<= name(n => Artifact(n, "war", "war")),
			publishArtifact in (Compile, packageBin) := false,
			exportedProducts <<= exportProductsTask		
		)
		
	def warSettings = inConfig(DefaultConf)(warSettings0) ++
		inConfig(Compile)(packageTasks(packageJar, packageBinTask)) ++
		addArtifact(artifact in (DefaultConf, packageBin), packageBin in DefaultConf) ++ Seq(
			
			`package` <<= packageBin in DefaultConf)
}
