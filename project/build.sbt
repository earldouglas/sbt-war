// bintray for publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// scripted for plugin testing
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
