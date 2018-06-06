scalaVersion := "2.12.5"

libraryDependencies += "com.github.spullara.mustache.java" % "compiler"              % "0.9.5"
libraryDependencies += "com.github.spullara.mustache.java" % "scala-extensions-2.12" % "0.9.5"
libraryDependencies += "javax.servlet"                     % "javax.servlet-api"     % "3.1.0" % "provided"

enablePlugins(JettyPlugin)

webappWebInfClasses := true
