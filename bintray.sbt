import bintray.Keys._

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

licenses += ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))

bintrayOrganization in bintray := None

