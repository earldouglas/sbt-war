// bintray for publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// scripted for plugin testing
libraryDependencies <+= sbtVersion(v => "org.scala-sbt" % "scripted-plugin" % v)
