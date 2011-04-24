import sbt._

import Project.Initialize
import Keys._
import Defaults._

object WebPlugin extends Plugin {
  val jettyConf = config("jetty") hide

  val temporaryWarPath = SettingKey[Path]("temporary-war-path")
  val webappResources = SettingKey[PathFinder]("webapp-resources")
  val prepareWebapp = TaskKey[Unit]("prepare-webapp")
  val jettyConfiguration = TaskKey[JettyConfiguration]("jetty-configuration")
  private var _jettyInstance: Option[JettyRunner] = None
  val jettyInstance = TaskKey[JettyRunner]("jetty-instance")
  val jettyRun = TaskKey[Unit]("jetty-run")
  val jettyStop = TaskKey[Unit]("jetty-stop")
  val jettyReload = TaskKey[Unit]("jetty-reload")

  def prepareWebappTask(webappContents: PathFinder, warPath: Path, classpath: PathFinder, defaultExcludes: FileFilter, slog: Logger) {
    val log = slog.asInstanceOf[AbstractLogger]
    def descendents(parent: PathFinder, include: FileFilter) = parent.descendentsExcept(include, defaultExcludes)
    val extraJars = Path.emptyPathFinder
    val ignore = Path.emptyPathFinder
    import sbt.classpath.ClasspathUtilities
    val webInfPath = warPath / "WEB-INF"
    val webLibDirectory = webInfPath / "lib"
    val classesTargetDirectory = webInfPath / "classes"

    val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
    val classesAndResources = descendents(Path.lazyPathFinder(directories) ###, "*")
    if(log.atLevel(Level.Debug))
      directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

    import sbt.oldcompat.{copy, copyFlat, copyFilesFlat, clean}
    (copy(webappContents.get, warPath, log).right flatMap { copiedWebapp =>
      copy(classesAndResources.get, classesTargetDirectory, log).right flatMap { copiedClasses =>
	copyFlat(libs, webLibDirectory, log).right flatMap { copiedLibs =>
	  copyFilesFlat(extraJars.getFiles, webLibDirectory, log).right flatMap { copiedExtraLibs => {
	    val toRemove = scala.collection.mutable.HashSet(((warPath ** "*") --- ignore).get.toSeq : _*)
	    toRemove --= copiedWebapp
	    toRemove --= copiedClasses
	    toRemove --= copiedLibs
	    toRemove --= copiedExtraLibs
	    val (directories, files) = toRemove.toList.partition(_.isDirectory)
            if(log.atLevel(Level.Debug))
	      files.foreach(r => log.debug("Pruning file " + r))
	    val result =
	      clean(files, true, log) orElse {
		val emptyDirectories = directories.filter(directory => directory.asFile.listFiles.isEmpty)
                if(log.atLevel(Level.Debug))
		  emptyDirectories.foreach(r => log.debug("Pruning directory " + r))
		clean(emptyDirectories, true, log)
	      }
	    result.toLeft(())
	  }
	}}}}).left.toOption
  }

  def jettyConfigurationTask: Initialize[Task[JettyConfiguration]] = (fullClasspath in Runtime, managedClasspath in jettyConf, temporaryWarPath, scalaInstance, streams) map { (cp, jettyCp, warPath, scalaInstance, s) =>
    new DefaultJettyConfiguration {
      def classpath = Path.finder(cp.map(_.data))
      def jettyClasspath = Path.finder(jettyCp.map(_.data))
      def war = warPath
      def contextPath = "/"
      def classpathName = jettyConf.toString
      def parentLoader = scalaInstance.loader
      def scanDirectories = Seq(warPath)
      def scanInterval = JettyRunner.DefaultScanInterval
      def port = JettyRunner.DefaultPort
      def log = s.log.asInstanceOf[AbstractLogger]
      def jettyEnv = None
      def webDefaultXml = None
    }
  }
    

  def jettyInstanceTask: Initialize[Task[JettyRunner]] = (jettyConfiguration) map { (conf) =>    
    if(_jettyInstance.isEmpty) 
      _jettyInstance = Some(new JettyRunner(conf))
    _jettyInstance.get
  }
  
  def jettyRunTask: Initialize[Task[Unit]] = (prepareWebapp, jettyInstance) map { (pw, instance) =>
    instance() foreach error
  }

  def jettyStopTask: Initialize[Task[Unit]] = (jettyInstance) map { (instance) =>
    instance.stop()
    _jettyInstance = None
  }

  def jettyReloadTask: Initialize[Task[Unit]] = (jettyInstance) map { (instance) =>
    instance.reload()                                      
  }
  
  val webSettings = Seq(
    ivyConfigurations += jettyConf,
    temporaryWarPath <<= (target){ (target) => Path.fromFile(target / "webapp") },
    webappResources <<= (sourceDirectory in Runtime, defaultExcludes) {
      (sd, defaultExcludes) =>
        ((sd / "webapp") ###).descendentsExcept("*", defaultExcludes)
    },
    prepareWebapp <<= (compile in Runtime, copyResources in Runtime, webappResources, temporaryWarPath, fullClasspath in Runtime, defaultExcludes, streams) map {
      (c, r, w, wp, cp, excludes, s) =>
        prepareWebappTask(w, wp, Path.finder(cp.map(_.data)), excludes, s.log) },
    managedClasspath in jettyConf <<= (classpathTypes, update) map { (ct, report) => Classpaths.managedJars(jettyConf, ct, report)},
    jettyConfiguration <<= jettyConfigurationTask,
    jettyInstance <<= jettyInstanceTask,
    jettyRun <<= jettyRunTask,
    jettyStop <<= jettyStopTask,
    jettyReload <<= jettyReloadTask
  )
}
