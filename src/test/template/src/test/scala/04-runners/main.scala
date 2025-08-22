package runners

import munit.FunSuite

import java.sql.Connection
import java.sql.DriverManager

class MainSute extends FunSuite:

  test("unsafeIncrementAndGet()") {

    val main: Main =
      new Main:
        override lazy val c: Connection =
          Class.forName("org.h2.Driver")
          val connection: Connection =
            DriverManager.getConnection("jdbc:h2:mem:", "sa", "")
          connection.setAutoCommit(false)
          connection

    assertEquals(
      obtained = main.unsafeIncrementAndGet(),
      expected = 1
    )

    assertEquals(
      obtained = main.unsafeIncrementAndGet(),
      expected = 2
    )

    assertEquals(
      obtained = main.unsafeIncrementAndGet(),
      expected = 3
    )

    assertEquals(
      obtained = main.unsafeIncrementAndGet(),
      expected = 4
    )
  }

  test("unsafeIncrementAndGetAsJson()") {

    val main: Main =
      new Main:
        override lazy val c: Connection =
          Class.forName("org.h2.Driver")
          val connection: Connection =
            DriverManager.getConnection("jdbc:h2:mem:", "sa", "")
          connection.setAutoCommit(false)
          connection

    assertEquals(
      obtained = main.unsafeIncrementAndGetAsJson(),
      expected =
          """|{
             |  "count": 1
             |}
             |""".stripMargin
    )

    assertEquals(
      obtained = main.unsafeIncrementAndGetAsJson(),
      expected =
          """|{
             |  "count": 2
             |}
             |""".stripMargin
    )

    assertEquals(
      obtained = main.unsafeIncrementAndGetAsJson(),
      expected =
          """|{
             |  "count": 3
             |}
             |""".stripMargin
    )

  }
