package com.earldouglas

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.collection.JavaConverters._

class WebappComponentsConfigurationTest
    extends AnyFunSuite
    with Matchers
    with BeforeAndAfterAll {

  test("load") {

    val configuration: WebappComponentsConfiguration =
      WebappComponentsConfiguration
        .load("src/test/resources/webapp-components.properties")

    configuration.hostname shouldBe "localhost"
    configuration.port shouldBe 8989
    configuration.emptyWebappDir shouldBe (new File("target/empty"))
    configuration.emptyClassesDir shouldBe (new File("target/empty"))
    configuration.resourceMap.asScala shouldBe
      List("bar.html", "foo.html", "baz/raz.css")
        .map(x =>
          (x -> new File(s"src/test/fakeproject/src/main/webapp/${x}"))
        )
        .toMap
  }
}
