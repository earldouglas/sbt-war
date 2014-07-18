organization := "test"

lazy val root = (project in file(".")) aggregate(maths, mathsweb, typeclasses)

lazy val maths = project

lazy val typeclasses = project

lazy val mathsweb = project webappDependsOn (maths, typeclasses)
