libraryDependencies += "com.github.spullara.mustache.java" % "scala-extensions-2.12" % "0.9.10"
libraryDependencies += "com.github.spullara.mustache.java" % "compiler" % "0.9.10"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

enablePlugins(JettyPlugin)

webappWebInfClasses := true
