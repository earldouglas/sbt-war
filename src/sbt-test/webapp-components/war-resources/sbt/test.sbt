enablePlugins(WebappComponentsPlugin)

////////////////////////////////////////////////////////////////////////

TaskKey[Unit]("check") := {

  val log: sbt.internal.util.ManagedLogger = streams.value.log

  def assertEquals(
      name: String,
      expected: Map[String, File],
      obtained: Map[String, File]
  ): Unit = {

    val sizesDoNotMatch = expected.size != obtained.size
    val mappingsDoNotMatch = expected != obtained

    if (sizesDoNotMatch || mappingsDoNotMatch) {
      log.error(name)
      sys.error(
        s"""|${name}
            |  expected:
            |${expected.mkString("    - ", "\n    - ", "")}
            |  obtained:
            |${obtained.mkString("    - ", "\n    - ", "")}
            |""".stripMargin
      )
    } else {
      log.success(name)
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
    name = "WebappComponentsPlugin: warResources",
    expected = {
      val root: File = (Compile / sourceDirectory).value
      expected
        .map(x => x -> root / "webapp" / x)
        .toMap
    },
    obtained = (Runtime / warResources).value
  )
}
