scriptedBufferLog := false

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

/* Replace the default scripted task so we can run certain tests once for each
 * supported servlet container.
 */
scripted <<= InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
  (scriptedDependencies, scriptedTests, scriptedRun, sbtTestDirectory, scriptedBufferLog, scriptedSbt, scalaVersion, sbtLauncher, scriptedLaunchOpts, result) map {
    (deps, m, r, testdir, bufferlog, version, scalaVersion, launcher, launchOpts, args) => {
      // Due to an API change in SBT 0.13 we have to try 2 argument lists when trying to run scripted tests
      def runTests(tests: Array[String], options: Array[String]): Unit = {
        try {
          // SBT 0.13
          r.invoke(m, testdir, bufferlog: java.lang.Boolean, tests, launcher, options)
        } catch {
          case iae: IllegalArgumentException =>
              // SBT 0.12
              r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scalaVersion, scalaVersion, tests, launcher, options)
        }
      }
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
          runTests(regularTests.toArray, launchOpts.toArray)
        }
        // run the shared container tests once for each supported container
        if(!containerTests.isEmpty) {
          val supportedContainers = Seq("jetty6", "jetty7", "jetty8", "jetty9", "tomcat")
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
            runTests(containerTests.toArray, opts)
          }
        }
      } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
    }
  }
}
