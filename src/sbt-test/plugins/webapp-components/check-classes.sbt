TaskKey[Unit]("check-classes") := {

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
      "domain/entities/Count$.class",
      "domain/entities/Count.class",
      "domain/entities/Count.tasty",
      "domain/operations/Counter.class",
      "domain/operations/Counter.tasty",
      "drivers/db/Transactor.class",
      "drivers/db/Transactor.tasty",
      "drivers/db/db$package$$anon$1$$anon$2.class",
      "drivers/db/db$package$$anon$1.class",
      "drivers/db/db$package$.class",
      "drivers/db/db$package.class",
      "drivers/db/db$package.tasty",
      "drivers/mem/mem$package$$anon$1$$anon$2.class",
      "drivers/mem/mem$package$$anon$1.class",
      "drivers/mem/mem$package$.class",
      "drivers/mem/mem$package.class",
      "drivers/mem/mem$package.tasty",
      "runners/Main$$anon$1.class",
      "runners/Main$tx$.class",
      "runners/Main.class",
      "runners/Main.tasty",
      "runners/MainServlet.class",
      "runners/MainServlet.tasty",
      "usecases/Counter.class",
      "usecases/Counter.tasty"
    )

  assertEquals(
    name = "classes",
    expected = {
      val root: File = (Compile / classDirectory).value
      expected
        .map(x => root / x -> x)
        .toMap
    },
    obtained = webappClasses.value
  )
}
