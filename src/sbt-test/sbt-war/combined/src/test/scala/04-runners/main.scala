package runners

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.sql.Connection
import java.sql.DriverManager

class MainSute extends AnyFunSuite with Matchers:

  test("unsafeIncrementAndGet()") {

    val main: Main =
      new Main:
        override lazy val c: Connection =
          Class.forName("org.h2.Driver")
          val connection: Connection =
            DriverManager.getConnection("jdbc:h2:mem:", "sa", "")
          connection.setAutoCommit(false)
          connection

    main.unsafeIncrementAndGet() shouldBe 1
    main.unsafeIncrementAndGet() shouldBe 2
    main.unsafeIncrementAndGet() shouldBe 3
    main.unsafeIncrementAndGet() shouldBe 4
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

    main.unsafeIncrementAndGetAsJson() shouldBe
      """|{
         |  "count": 1
         |}
         |""".stripMargin

    main.unsafeIncrementAndGetAsJson() shouldBe
      """|{
         |  "count": 2
         |}
         |""".stripMargin

    main.unsafeIncrementAndGetAsJson() shouldBe
      """|{
         |  "count": 3
         |}
         |""".stripMargin
  }
