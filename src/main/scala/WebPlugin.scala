import sbt._

import Project.Initialize
import Keys._
import Defaults._

object WebPlugin extends Plugin {
  val jettyConf = config("jetty") hide

  val temporaryWarPath = SettingKey[Path]("temporary-war-path")
  val webappResources = SettingKey[PathFinder]("webapp-resources")
  val webappUnmanaged = SettingKey[Seq[File]]("webapp-unmanaged")
  val prepareWebapp = TaskKey[Unit]("prepare-webapp")
  val jettyClasspaths = TaskKey[JettyClasspaths]("jetty-classpaths")
  final case class JettyClasspaths(classpath: PathFinder, jettyClasspath: PathFinder)
  val jettyContext = SettingKey[String]("jetty-context")
  val jettyScanDirs = SettingKey[Seq[Path]]("jetty-scan-dirs")
  val jettyScanInterval = SettingKey[Int]("jetty-scan-interval")
  val jettyPort = SettingKey[Int]("jetty-port")
  val jettyConfFiles = SettingKey[JettyConfFiles]("jetty-conf-files")
  final case class JettyConfFiles(env: Option[File], webDefaultXml: Option[File])
  val jettyConfiguration = TaskKey[JettyConfiguration]("jetty-configuration")
  private var _jettyInstance: Option[JettyRunner] = None
  val jettyInstance = TaskKey[JettyRunner]("jetty-instance")
  val jettyRun = TaskKey[Unit]("jetty-run")
  val jettyStop = TaskKey[Unit]("jetty-stop")
  val jettyReload = TaskKey[Unit]("jetty-reload")

  def prepareWebappTask(webappContents: PathFinder, warPath: Path, classpath: PathFinder, extraJars: PathFinder, ignore: PathFinder, defaultExcludes: FileFilter, slog: Logger) {
    val log = slog.asInstanceOf[AbstractLogger]
    def descendents(parent: PathFinder, include: FileFilter) = parent.descendentsExcept(include, defaultExcludes)
    import sbt.classpath.ClasspathUtilities
    val webInfPath = warPath / "WEB-INF"
    val webLibDirectory = webInfPath / "lib"
    val classesTargetDirectory = webInfPath / "classes"

    val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
    val classesAndResources = descendents(Path.lazyPathFinder(directories) ###, "*")
    if(log.atLevel(Level.Debug))
      directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

    import sbt.oldcompat.{copy, copyFlat, clean}
    (copy(webappContents.get, warPath, log).right flatMap { copiedWebapp =>
      copy(classesAndResources.get, classesTargetDirectory, log).right flatMap { copiedClasses =>
	copyFlat(libs ++ extraJars.get, webLibDirectory, log).right flatMap { copiedLibs =>
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
	  }
	}}).left.toOption foreach error
  }

  def jettyClasspathsTask(cp: Classpath, jettyCp: Classpath) =
    JettyClasspaths(cp.map(_.data), jettyCp.map(_.data))

  def jettyConfigurationTask: Initialize[Task[JettyConfiguration]] = (jettyClasspaths, temporaryWarPath, jettyContext, scalaInstance, jettyScanDirs, jettyScanInterval, jettyPort, jettyConfFiles, streams) map { (classpaths, warPath, context, scalaInstance, scanDirs, interval, jettyPort, confs, s) =>
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
      def log = s.log.asInstanceOf[AbstractLogger]
      def jettyEnv = confs.env
      def webDefaultXml = confs.webDefaultXml
    }
  }
    

  def jettyInstanceTask: Initialize[Task[JettyRunner]] = (jettyConfiguration) map {
    (conf) =>
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
    webappUnmanaged := Seq(),
    prepareWebapp <<= (compile in Runtime, copyResources in Runtime, webappResources, temporaryWarPath, jettyClasspaths, scalaInstance, webappUnmanaged, defaultExcludes, streams) map {
      (c, r, w, wp, cp, si, wu, excludes, s) =>
        prepareWebappTask(w, wp, cp.classpath, Seq(si.libraryJar, si.compilerJar), wu, excludes, s.log) },
    managedClasspath in jettyConf <<= (classpathTypes, update) map { (ct, report) => Classpaths.managedJars(jettyConf, ct, report)},
    jettyClasspaths <<= (fullClasspath in Runtime, managedClasspath in jettyConf) map jettyClasspathsTask,
    jettyContext := "/",
    jettyScanDirs <<= (temporaryWarPath) { (warPath) => Seq(warPath) },
    jettyScanInterval := JettyRunner.DefaultScanInterval,
    jettyPort := JettyRunner.DefaultPort,
    jettyConfFiles := JettyConfFiles(None, None),
    jettyConfiguration <<= jettyConfigurationTask,
    jettyInstance <<= jettyInstanceTask,
    jettyRun <<= jettyRunTask,
    jettyStop <<= jettyStopTask,
    jettyReload <<= jettyReloadTask
  )
}
