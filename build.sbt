// general
organization  := "com.earldouglas"
name          := "xsbt-web-plugin"

scalacOptions    ++= Seq("-feature", "-deprecation")
crossSbtVersions  := Seq("0.13.6", "1.0.0")
sbtPlugin         := true

// bintray-sbt
publishMavenStyle := false
licenses          += ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))

// scripted-plugin
ScriptedPlugin.scriptedSettings
scriptedBufferLog   := false
scriptedLaunchOpts  += { "-Dplugin.version=" + version.value }
watchSources       ++= { (sourceDirectory.value ** "*").get }

// AWS deployment support
libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % "1.11.105"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3"               % "1.11.105"

// sbt-pgp
useGpg := true
