organization := "test"

val commonSettings = Seq(
  scalaVersion := "2.10.6"
)

lazy val root = (project in file(".")).aggregate(maths, mathsweb, remote, remoteweb)
  .settings(commonSettings: _*)

lazy val maths = project
  .settings(commonSettings: _*)

lazy val remote = project
  .settings(commonSettings: _*)

lazy val remoteweb = project.dependsOn(remote)
  .settings(commonSettings: _*)

lazy val mathsweb = project.dependsOn(maths)
  .settings(commonSettings: _*)
