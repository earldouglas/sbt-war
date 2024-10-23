libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "com.h2database" % "h2" % "2.2.224"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.8"

scalaVersion := "3.5.0"
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalacOptions += "-Wunused:all"
