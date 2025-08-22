package com.earldouglas

import munit.FunSuite

import java.io.File
import scala.collection.JavaConverters._

class WebappComponentsConfigurationTest extends FunSuite {

  test("load") {

    val configuration: WebappComponentsConfiguration =
      WebappComponentsConfiguration
        .load("src/test/resources/webapp-components.properties")

    assertEquals(
      obtained = configuration.hostname,
      expected = "localhost"
    )

    assertEquals(
      obtained = configuration.port,
      expected = 8989
    )

    assertEquals(
      obtained = configuration.emptyWebappDir,
      expected = new File("target/empty")
    )

    assertEquals(
      obtained = configuration.emptyClassesDir,
      expected = new File("target/empty")
    )

    assertEquals(
      obtained = configuration.resourceMap.asScala.toMap,
      expected = List("bar.html", "foo.html", "baz/raz.css")
        .map(x =>
          (x -> new File(s"src/test/fakeproject/src/main/webapp/${x}"))
        )
        .toMap
    )
  }
}
