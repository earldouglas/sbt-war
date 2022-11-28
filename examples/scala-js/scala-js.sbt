enablePlugins(ScalaJSPlugin)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.3.0"

// Send generated JS to the XWP webapp dir
crossTarget in fastOptJS := (target in webappPrepare).value
crossTarget in fullOptJS := (target in webappPrepare).value

// Don't append -fastopt/-fullopt to the generated .js filename
artifactPath in (Compile, fastOptJS) :=
  ((crossTarget in fastOptJS).value / ((moduleName in fastOptJS).value + ".js"))
artifactPath in (Compile, fullOptJS) :=
  ((crossTarget in fullOptJS).value / ((moduleName in fullOptJS).value + ".js"))
