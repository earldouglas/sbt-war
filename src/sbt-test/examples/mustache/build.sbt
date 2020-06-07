scalaVersion := "2.12.11"

libraryDependencies += "com.github.spullara.mustache.java" % "compiler"              % "0.9.5"
libraryDependencies += "com.github.spullara.mustache.java" % "scala-extensions-2.12" % "0.9.5"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

enablePlugins(JettyPlugin)

webappWebInfClasses := true
