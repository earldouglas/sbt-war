organization := "com.earldouglas"

name := "xsbt-web-plugin"

version := "0.4.0-SNAPSHOT"

scalaVersion := "2.10.2"

crossScalaVersions := Seq(/*"2.9.0", "2.9.1", "2.9.2", "2.9.3", */"2.10.0", "2.10.1", "2.10.2")

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.mortbay.jetty"       % "jetty"                     % "6.1.22"          % "optional",
  "org.mortbay.jetty"       % "jetty-plus"                % "6.1.22"          % "optional",
  "org.eclipse.jetty"       % "jetty-webapp"              % "7.5.1.v20110908" % "optional",
  "org.eclipse.jetty"       % "jetty-plus"                % "7.5.1.v20110908" % "optional",
  "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22"          % "optional",
  "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22"          % "optional",
  "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22"          % "optional"
)

scalacOptions += "-deprecation"

scriptedBufferLog := false

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

/* Replace the default scripted task so we can run certain tests once for each
 * supported servlet container.
 */
scripted <<= InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
  (scriptedDependencies, scriptedTests, scriptedRun, sbtTestDirectory, scriptedBufferLog, scriptedSbt, scriptedScalas, sbtLauncher, scriptedLaunchOpts, result) map {
    (deps, m, r, testdir, bufferlog, version, scriptedScalas, launcher, launchOpts, args) => {
      // If we don't get any test arguments we need to find all of the tests
      // so we can separate out the shared container tests.  Otherwise scripted
      // will just run all of the tests once.
      val tests: Seq[String] = if(args.isEmpty) {
        for(group <-testdir.listFiles; 
            if group.isDirectory;
            test <- group.listFiles;
            if test.isDirectory) yield { group.name + "/" + test.name }
      } else {
        args
      }
      // Separate out the shared container tests
      val (containerTests, regularTests) = tests.partition(_.startsWith("webapp-common"))
      try {
        // Run the regular tests once
        if(!regularTests.isEmpty) {
          r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, regularTests.toArray, launcher, launchOpts.toArray)
        }
        // run the shared container tests once for each supported container
        if(!containerTests.isEmpty) {
          val supportedContainers = Seq("jetty6", "jetty7"/*, "tomcat"*/)
          supportedContainers.foreach { container =>
            println("===== Shared container tests for " + container + " =====")
            val containerOpt = "-Dplugin.container=" + container
            // We have to pass the location of the test dir to the tests so
            // they can find shared sources.  This is only necessary when
            // running them through scripted since the scripted plugin runs
            // the tests in a temp dir.  When running the tests directly we
            // just use a relative path.
            val commonTestDirOpt = "-Dplugin.webapp.common.dir=" + testdir.getPath + "/"
            val opts = launchOpts.toArray :+ containerOpt :+ commonTestDirOpt
            r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, containerTests.toArray, launcher, opts)
          }
        }
      } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
    }
  }
}
