name := "test"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.3"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
libraryDependencies += "javax.annotation" % "javax.annotation-api" % "1.3.2" % "provided" // for @PostConstruct as of Java 11
