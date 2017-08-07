organization := "test"

val commonSettings = Seq(
  scalaVersion := "2.10.6"
)

lazy val root = (project in file(".")).aggregate(numbers, maths, mathsweb, typeclasses)
  .settings(commonSettings: _*)

lazy val numbers = project
  .settings(commonSettings: _*)

lazy val maths = project.dependsOn(numbers)
  .settings(commonSettings: _*)

lazy val typeclasses = project
  .settings(commonSettings: _*)

lazy val mathsweb = project.dependsOn(maths, typeclasses)
  .settings(commonSettings: _*)
