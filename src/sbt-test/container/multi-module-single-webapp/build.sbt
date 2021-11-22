lazy val root = (project in file(".")).aggregate(
  numbers,
  maths,
  mathsweb,
  typeclasses
)

lazy val numbers = project

lazy val maths = project.dependsOn(numbers)

lazy val typeclasses = project

lazy val mathsweb = project.dependsOn(maths, typeclasses)
