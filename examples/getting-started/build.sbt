name := "xwp-template"

organization := "com.earldouglas"

scalaVersion := "2.11.7"

enablePlugins(JettyPlugin)


/*
enablePlugins(HerokuDeploy)

herokuAppName := "xwp-template"
*/

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies ++= Seq( // test
    "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "test"
  , "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "test"
  , "javax.servlet" % "javax.servlet-api" % "3.1.0" % "test"
  , "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
