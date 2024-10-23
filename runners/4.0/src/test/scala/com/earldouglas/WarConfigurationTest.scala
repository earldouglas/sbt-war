package com.earldouglas

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File

class WarConfigurationTest
    extends AnyFunSuite
    with Matchers
    with BeforeAndAfterAll {

  test("load") {

    val configuration: WarConfiguration =
      WarConfiguration.load("src/test/resources/war.properties")

    configuration.port shouldBe 8988

    configuration.warFile shouldBe
      new File("src/test/fakeproject/src/main/webapp")
  }
}
