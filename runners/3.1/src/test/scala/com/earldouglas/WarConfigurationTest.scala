package com.earldouglas

import munit.FunSuite

import java.io.File

class WarConfigurationTest extends FunSuite {

  test("load") {

    val configuration: WarConfiguration =
      WarConfiguration.load("src/test/resources/war.properties")

    assertEquals(
      obtained = configuration.port,
      expected = 8801
    )

    assertEquals(
      obtained = configuration.warFile,
      expected = new File("src/test/fakeproject/src/main/webapp")
    )
  }
}
