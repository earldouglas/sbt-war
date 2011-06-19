package com.github.siasia

import _root_.sbt._

import Project.Initialize
import Keys._
import Defaults._
import Scope.GlobalScope

object WebPlugin extends Plugin {
	val jettyConf = config("jetty") hide

	val temporaryWarPath = SettingKey[File]("temporary-war-path")
	val webappResources = SettingKey[PathFinder]("webapp-resources")
	val watchWebappResources = TaskKey[Seq[File]]("watch-webapp-resources")
	val webappUnmanaged = SettingKey[PathFinder]("webapp-unmanaged")
	val prepareWebapp = TaskKey[Seq[(File, String)]]("prepare-webapp")
	val packageWar = TaskKey[File]("package-war")
	val jettyClasspaths = TaskKey[JettyClasspaths]("jetty-classpaths")
	final case class JettyClasspaths(classpath: PathFinder, jettyClasspath: PathFinder)
	val jettyContext = SettingKey[String]("jetty-context")
	val jettyScanDirs = SettingKey[Seq[File]]("jetty-scan-dirs")
	val jettyScanInterval = SettingKey[Int]("jetty-scan-interval")
	val jettyPort = SettingKey[Int]("jetty-port")
	val jettyConfFiles = SettingKey[JettyConfFiles]("jetty-conf-files")
	final case class JettyConfFiles(env: Option[File], webDefaultXml: Option[File])
	val jettyConfiguration = TaskKey[JettyConfiguration]("jetty-configuration")
	val jettyInstances = AttributeKey[Map[ProjectRef,JettyRunner]]("jetty-instance")

	def prepareWebappTask(webappContents: PathFinder, warPath: File, classpath: PathFinder, ignore: PathFinder, defaultExcludes: FileFilter, slog: Logger): Seq[(File, String)] = {
		val log = slog.asInstanceOf[AbstractLogger]    
		import _root_.sbt.classpath.ClasspathUtilities
		val webInfPath = warPath / "WEB-INF"
		val webLibDirectory = webInfPath / "lib"
		val classesTargetDirectory = webInfPath / "classes"

		val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
		val wcToCopy = for {
			dir <- webappContents.get
			file <- dir.descendentsExcept("*", defaultExcludes).get
			val target = Path.rebase(dir, warPath)(file).get
		} yield (file, target)
		val classesAndResources = for {
			dir <- directories
			file <- dir.descendentsExcept("*", defaultExcludes).get
			val target = Path.rebase(dir, classesTargetDirectory)(file).get
		} yield (file, target)
		if(log.atLevel(Level.Debug))
			directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

		import sbt.oldcompat.copyFlat
		val copiedWebapp = IO.copy(wcToCopy)
		val copiedClasses = IO.copy(classesAndResources)
		val copiedLibs = copyFlat(libs, webLibDirectory)
		val toRemove = scala.collection.mutable.HashSet(((warPath ** "*") --- ignore).get.toSeq : _*)
		toRemove --= copiedWebapp
		toRemove --= copiedClasses
		toRemove --= copiedLibs
		val (dirs, files) = toRemove.toList.partition(_.isDirectory)
		if(log.atLevel(Level.Debug))
			files.foreach(r => log.debug("Pruning file " + r))
		IO.delete(files)
		IO.deleteIfEmpty(dirs.toSet)
		((warPath).descendentsExcept("*", defaultExcludes) --- ignore) x (relativeTo(warPath)|flat)
	}

	def packageWarTask: Initialize[Task[Seq[(File, String)]]] = prepareWebapp map { (pw) => pw }

	def jettyClasspathsTask(cp: Classpath, jettyCp: Classpath) =
		JettyClasspaths(cp.map(_.data), jettyCp.map(_.data))

	def jettyConfigurationTask: Initialize[Task[JettyConfiguration]] = (jettyClasspaths, temporaryWarPath, jettyContext, scalaInstance, jettyScanDirs, jettyScanInterval, jettyPort, jettyConfFiles, state) map {
		(classpaths, warPath, context, scalaInstance, scanDirs, interval, jettyPort, confs, state) =>
			new DefaultJettyConfiguration {
				def classpath = classpaths.classpath
				def jettyClasspath = classpaths.jettyClasspath
				def war = warPath
				def contextPath = context
				def classpathName = jettyConf.toString
				def parentLoader = scalaInstance.loader
				def scanDirectories = scanDirs
				def scanInterval = interval
				def port = jettyPort
				def log = CommandSupport.logger(state).asInstanceOf[AbstractLogger]
				def jettyEnv = confs.env
				def webDefaultXml = confs.webDefaultXml
			}
	}

	def addJettyInstance(state: State): State = {
		val extracted: Extracted = Project.extract(state)
		import extracted._
		val instances = state.get(jettyInstances) getOrElse(Map())
		instances.get(currentRef) match {
			case Some(_) => state
			case None =>
				val result = Project.evaluateTask(jettyConfiguration in Compile, state) getOrElse error("Failed to get jetty configuration.")
				val conf = EvaluateTask.processResult(result, CommandSupport.logger(state))
				val instance = new JettyRunner(conf)			
				state.addExitHook(instance.runBeforeExiting).put(jettyInstances, instances + (currentRef -> instance))
		}
		
	}

	def getInstance(state: State): JettyRunner = {
		val extracted: Extracted = Project.extract(state)
		import extracted._
		state.get(jettyInstances).get.apply(currentRef)
	}

	def withJettyInstance(action: (JettyRunner) => Unit)(state: State): State = {
		val withInstance = addJettyInstance(state)
		action(getInstance(withInstance))
		withInstance
	}

	def jettyRunAction(state: State): State = {
		val withInstance = addJettyInstance(state)
		val result = Project.evaluateTask(prepareWebapp, withInstance) getOrElse error("Cannot prepare webapp.")
		EvaluateTask.processResult(result, CommandSupport.logger(withInstance))
		getInstance(withInstance).apply()
		withInstance
	}

	val jettyRun: Command = Command.command("jetty-run")(jettyRunAction)
	val jettyStop: Command = Command.command("jetty-stop")(withJettyInstance(_.stop()))
	val jettyReload: Command = Command.command("jetty-reload")(withJettyInstance(_.reload()))

	val webSettings: Seq[Project.Setting[_]] = Seq(
		ivyConfigurations += jettyConf,
		temporaryWarPath <<= (target){ (target) => target / "webapp" },
		webappResources <<= (sourceDirectory in Runtime, defaultExcludes) {
			(sd, defaultExcludes) =>
				sd / "webapp"
		},
		watchWebappResources <<= (webappResources, defaultExcludes) map { (rs, de) => rs.descendentsExcept("*", de).get },
		watchSources <<= Seq(watchSources, watchWebappResources).join.map { _.map(_.flatten.distinct) },
		webappUnmanaged := PathFinder.empty,
		prepareWebapp <<= (copyResources in Runtime, webappResources, temporaryWarPath, jettyClasspaths, webappUnmanaged, defaultExcludes, streams) map {
			(r, w, wp, cp, wu, excludes, s) =>
				prepareWebappTask(w, wp, cp.classpath, wu, excludes, s.log) },
		configuration in packageWar := Compile,
		artifact in packageWar <<= name(n => Artifact(n, "war", "war")),
		managedClasspath in jettyClasspaths <<= (classpathTypes, update) map { (ct, report) => Classpaths.managedJars(jettyConf, ct, report) },
		jettyClasspaths <<= (fullClasspath in Runtime, managedClasspath in jettyClasspaths) map jettyClasspathsTask,
		jettyContext := "/",
		jettyScanDirs <<= Seq(temporaryWarPath).join,
		jettyScanInterval := JettyRunner.DefaultScanInterval,
		jettyPort := JettyRunner.DefaultPort,
		jettyConfFiles := JettyConfFiles(None, None),
		jettyConfiguration <<= jettyConfigurationTask,
		commands ++= Seq(jettyRun, jettyStop, jettyReload)
	) ++ packageTasks(packageWar, packageWarTask)
}
