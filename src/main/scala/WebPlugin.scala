import sbt._

import Project.Initialize
import Keys._
import Defaults._
import Scope.GlobalScope

object WebPlugin extends Plugin {
	val jettyConf = config("jetty") hide

	val temporaryWarPath = SettingKey[Path]("temporary-war-path")
	val webappResources = SettingKey[PathFinder]("webapp-resources")
	val watchWebappResources = TaskKey[Seq[File]]("watch-webapp-resources")
	val webappUnmanaged = SettingKey[PathFinder]("webapp-unmanaged")
	val prepareWebapp = TaskKey[Seq[(File, String)]]("prepare-webapp")
	val jettyClasspaths = TaskKey[JettyClasspaths]("jetty-classpaths")
	final case class JettyClasspaths(classpath: PathFinder, jettyClasspath: PathFinder)
	val jettyContext = SettingKey[String]("jetty-context")
	val jettyScanDirs = SettingKey[Seq[Path]]("jetty-scan-dirs")
	val jettyScanInterval = SettingKey[Int]("jetty-scan-interval")
	val jettyPort = SettingKey[Int]("jetty-port")
	val jettyConfFiles = SettingKey[JettyConfFiles]("jetty-conf-files")
	final case class JettyConfFiles(env: Option[File], webDefaultXml: Option[File])
	val jettyConfiguration = TaskKey[JettyConfiguration]("jetty-configuration")
	val jettyInstance = AttributeKey[JettyRunner]("jetty-instance")

	def prepareWebappTask(webappContents: PathFinder, warPath: Path, classpath: PathFinder, extraJars: PathFinder, ignore: PathFinder, defaultExcludes: FileFilter, slog: Logger): Seq[(File, String)] = {
		val log = slog.asInstanceOf[AbstractLogger]    
		import sbt.classpath.ClasspathUtilities
		val webInfPath = warPath / "WEB-INF"
		val webLibDirectory = webInfPath / "lib"
		val classesTargetDirectory = webInfPath / "classes"

		val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
		val classesAndResources = (Path.lazyPathFinder(directories) ###).descendentsExcept("*", defaultExcludes)
		if(log.atLevel(Level.Debug))
			directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

		import sbt.oldcompat.{copy, copyFlat, clean}
		(copy(webappContents.get, warPath, log).right flatMap { copiedWebapp =>
			copy(classesAndResources.get, classesTargetDirectory, log).right flatMap { copiedClasses =>
				copyFlat(libs ++ extraJars.get, webLibDirectory, log).right flatMap {
					copiedLibs =>
						val toRemove = scala.collection.mutable.HashSet(((warPath ** "*") --- ignore).get.toSeq : _*)
					toRemove --= copiedWebapp
					toRemove --= copiedClasses
					toRemove --= copiedLibs
					val (directories, files) = toRemove.toList.partition(_.isDirectory)
					if(log.atLevel(Level.Debug))
						files.foreach(r => log.debug("Pruning file " + r))
					val result =
						clean(files, log) orElse {
							val emptyDirectories = directories.filter(directory => directory.asFile.listFiles.isEmpty)
							if(log.atLevel(Level.Debug))
								emptyDirectories.foreach(r => log.debug("Pruning directory " + r))
							clean(emptyDirectories, log)
						}
					result.toLeft(())
				}}}).left.toOption foreach error
		((warPath ###).descendentsExcept("*", defaultExcludes) --- ignore) x (relativeTo(warPath)|flat)
	}

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
		if(!state.get(jettyInstance).isEmpty)
			return state
		val result = Project.evaluateTask(jettyConfiguration in Compile, state) getOrElse error("Failed to get jetty configuration.")
		val conf = EvaluateTask.processResult(result, CommandSupport.logger(state))
		val instance = new JettyRunner(conf)
		state.addExitHook(instance.runBeforeExiting).put(jettyInstance, instance)
	}

	def withJettyInstance(action: (JettyRunner) => Unit)(state: State): State = {
		val withInstance = addJettyInstance(state)
		action(withInstance.get(jettyInstance).get)
		withInstance
	}

	def jettyRunAction(state: State): State = {
		val withInstance = addJettyInstance(state)
		val result = Project.evaluateTask(prepareWebapp, withInstance) getOrElse error("Cannot prepare webapp.")
		EvaluateTask.processResult(result, CommandSupport.logger(withInstance))
		withInstance.get(jettyInstance).get.apply()
		withInstance
	}



	val jettyRun: Command = Command.command("jetty-run")(jettyRunAction)
	val jettyStop: Command = Command.command("jetty-stop")(withJettyInstance(_.stop()))
	val jettyReload: Command = Command.command("jetty-reload")(withJettyInstance(_.reload()))

	val webSettings = Seq(
		ivyConfigurations += jettyConf,
		temporaryWarPath <<= (target){ (target) => Path.fromFile(target / "webapp") },
		webappResources <<= (sourceDirectory in Runtime, defaultExcludes) {
			(sd, defaultExcludes) =>
				((sd / "webapp") ###).descendentsExcept("*", defaultExcludes)
		},
		watchWebappResources <<= (webappResources) map { (rs) => rs.getFiles },
		watchSources <<= Seq(watchSources, watchWebappResources).join.map { _.map(_.flatten.distinct) },
		webappUnmanaged := Path.emptyPathFinder,
		prepareWebapp <<= (compile in Runtime, copyResources in Runtime, webappResources, temporaryWarPath, jettyClasspaths, scalaInstance, webappUnmanaged, defaultExcludes, streams) map {
			(c, r, w, wp, cp, si, wu, excludes, s) =>
				prepareWebappTask(w, wp, cp.classpath, Seq(si.libraryJar, si.compilerJar), wu, excludes, s.log) },
		mappings in (Compile, packageBin) <<= (prepareWebapp) map { (pw) => pw },
		packageOptions in (Compile, packageBin) <<= (packageOptions) map { po => po },
		artifact <<= name(n => Artifact(n, "war", "war")),
		managedClasspath in jettyClasspaths <<= (classpathTypes, update) map { (ct, report) => Classpaths.managedJars(jettyConf, ct, report) },
		jettyClasspaths <<= (fullClasspath in Runtime, managedClasspath in jettyClasspaths) map jettyClasspathsTask,
		jettyContext := "/",
		jettyScanDirs <<= Seq(temporaryWarPath).join,
		jettyScanInterval := JettyRunner.DefaultScanInterval,
		jettyPort := JettyRunner.DefaultPort,
		jettyConfFiles := JettyConfFiles(None, None),
		jettyConfiguration <<= jettyConfigurationTask,
		commands ++= Seq(jettyRun, jettyStop, jettyReload)
	)
}
