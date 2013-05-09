package com.github.siasia

import sbt.{`package` => _, _}
import Project.Initialize
import Keys._
import PluginKeys._
import _root_.sbt.Defaults.{packageTasks, packageBinTask, inDependencies}
import _root_.sbt.Classpaths.analyzed

object WarPlugin extends Plugin {
	private def copyFlat(sources: Iterable[File], destinationDirectory: File): Set[File] = {
    val map = sources.map(source => (source.asFile, destinationDirectory / source.getName))
    IO.copy(map)
  }
	
	def packageWarTask(classpathConfig: Configuration): Initialize[Task[Seq[(File, String)]]] =
		(webappResources, target, fullClasspath in classpathConfig, excludeFilter, warPostProcess, streams) map {
			(webappResources, target, fullClasspath, filter, postProcess, s) =>
			val classpath = fullClasspath.map(_.data)
			val warPath = target / "webapp"
			val log = s.log.asInstanceOf[AbstractLogger]    
			import _root_.sbt.classpath.ClasspathUtilities
			val webInfPath = warPath / "WEB-INF"
			val webLibDirectory = webInfPath / "lib"
			val classesTargetDirectory = webInfPath / "classes"

			val (libs, directories) = classpath.toList.partition(ClasspathUtilities.isArchive)
			val wcToCopy = for {
				dir <- webappResources.reverse
				file <- dir.descendentsExcept("*", filter).get
				val target = Path.rebase(dir, warPath)(file).get
			} yield (file, target)
			val classesAndResources = for {
				dir <- directories.reverse
				file <- dir.descendentsExcept("*", filter).get
				val target = Path.rebase(dir, classesTargetDirectory)(file).get
			} yield (file, target)
			if(log.atLevel(Level.Debug))
				directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

			val copiedWebapp = IO.copy(wcToCopy, overwrite = true, preserveLastModified = true)
			val copiedClasses = IO.copy(classesAndResources, overwrite = true, preserveLastModified = true)
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
			postProcess()
			(warPath).descendentsExcept("*", filter) x (relativeTo(warPath)|flat)
		}
	def warSettings0(classpathConfig: Configuration):Seq[Setting[_]] =
		packageTasks(packageWar, packageWarTask(classpathConfig)) ++ Seq(
			webappResources <<= sourceDirectory(sd => Seq(sd / "webapp")),
			webappResources <++= inDependencies(webappResources, ref => Nil, false) apply { _.flatten },
			artifact in packageWar <<= moduleName(n => Artifact(n, "war", "war")),
			publishArtifact in packageBin := false,
			warPostProcess := { () => () },
			`package` <<= packageWar)
	def warSettings0:Seq[Setting[_]] = warSettings0(DefaultClasspathConf)

	def globalWarSettings:Seq[Setting[_]] = Seq(addArtifact(artifact in (DefaultConf, packageWar), packageWar in DefaultConf) :_*)
		
	def warSettings(classpathConfig: Configuration):Seq[Setting[_]] = inConfig(DefaultConf)(warSettings0(classpathConfig)) ++ globalWarSettings
	def warSettings:Seq[Setting[_]] = warSettings(DefaultClasspathConf)
		
}
