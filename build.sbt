// general
organization  := "com.earldouglas"

name          := "xsbt-web-plugin"

sbtPlugin     := true

scalacOptions ++= Seq("-feature", "-deprecation")

crossSbtVersions := Seq("0.13.6", "1.0.0")

// bintray-sbt
publishMavenStyle := false

licenses          += ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))

// scripted-plugin
ScriptedPlugin.scriptedSettings

scriptedBufferLog  := false

scriptedLaunchOpts ++= Seq( "-Djavax.net.ssl.trustStore=" + (baseDirectory.value / "src/sbt-test/xsbt-web-plugin/container/etc/keystore").getPath
                          , "-Djavax.net.ssl.trustStorePassword=storepwd"
                          , "-Dplugin.version=" + version.value
                          )

watchSources       ++= { (sourceDirectory.value ** "*").get }

libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % "1.11.105"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3"               % "1.11.105"
