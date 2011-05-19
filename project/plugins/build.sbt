libraryDependencies <<= (libraryDependencies, appConfiguration) { (deps, app) => deps :+ "org.scala-tools.sbt" %% "scripted-plugin" % app.provider.id.version }
