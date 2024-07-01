TaskKey[Unit]("check-lib") := {

  def assertContains(
      name: String,
      expected: Set[String],
      obtained: Map[File, String]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained.values.toSet

    if (sizesDoNotMatch || mappingsDoNotMatch) {
      sys.error(
        s"""|${name}:
            |  expected: ${expected}
            |  obtained:
            |${obtained.mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    }
  }

  val expected: Set[String] =
    Set(
      "cats-core_3-2.9.0.jar",
      "cats-effect-kernel_3-3.5.4.jar",
      "cats-effect-std_3-3.5.4.jar",
      "cats-effect_3-3.5.4.jar",
      "cats-kernel_3-2.9.0.jar",
      "h2-2.2.224.jar",
      "scala-library-2.13.14.jar",
      "scala3-library_3-3.5.0.jar"
    )

  assertContains(
    name = "lib",
    expected = expected,
    obtained = webappLib.value
  )
}
