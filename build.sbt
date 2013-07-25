organization := "com.earldouglas"

name := "xsbt-web-plugin"

version := "0.4.0-SNAPSHOT"

crossScalaVersions := Seq("2.9.0", "2.9.1", "2.9.2", "2.9.3", "2.10.0", "2.10.1")

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

scripted <<= InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
  (scriptedDependencies, scriptedTests, scriptedRun, sbtTestDirectory, scriptedBufferLog, scriptedSbt, scriptedScalas, sbtLauncher, scriptedLaunchOpts, result) map {
    (deps, m, r, testdir, bufferlog, version, scriptedScalas, launcher, launchOpts, args) => {
      val tests: Seq[String] = if(args.isEmpty) {
        for(group <-testdir.listFiles; 
            if group.isDirectory;
            test <- group.listFiles;
            if test.isDirectory) yield { group.name + "/" + test.name }
      } else {
        args
      }
      val (containerTests, regularTests) = tests.partition(_.startsWith("webapp-common"))
      try {
        if(!regularTests.isEmpty) {
          r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, regularTests.toArray, launcher, launchOpts.toArray)
        }
        if(!containerTests.isEmpty) {
          val supportedContainers = Seq("jetty6", "jetty7"/*, "tomcat"*/)
          supportedContainers.foreach { container =>
            println("===== Shared container tests for " + container + " =====")
            val containerOpt = "-Dplugin.container=" + container
            val commonTestDirOpt = "-Dplugin.webapp.common.dir=" + testdir.getPath + "/"
            val opts = launchOpts.toArray :+ containerOpt :+ commonTestDirOpt
            r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, containerTests.toArray, launcher, opts)
          }
        }
      } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
    }
  }
}
