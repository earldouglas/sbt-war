TaskKey[Unit]("check-resources") := {

  def assertEquals(
      name: String,
      expected: Map[File, String],
      obtained: Map[File, String]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained

    if (sizesDoNotMatch || mappingsDoNotMatch) {
      sys.error(
        s"""|${name}:
            |  expected:
            |${expected.mkString("    - ", "\n    - ", "")}
            |  obtained:
            |${obtained.mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    }
  }

  val expected: List[String] =
    List(
      "WEB-INF/web.xml",
      "favicon.ico",
      "index.html",
      "styles/theme.css"
    )

  assertEquals(
    name = "resources",
    expected = {
      val root: File = (Compile / sourceDirectory).value
      expected
        .map(x => root / "webapp" / x -> x)
        .toMap
    },
    obtained = webappResources.value
  )
}
