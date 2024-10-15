package com.earldouglas.sbt.war

import java.net.HttpURLConnection
import java.net.URI
import scala.jdk.CollectionConverters._
import scala.io.Source
import java.util.{List => JavaList}
import java.util.{Map => JavaMap}

object TestCompat:

  def asScala[A, B](javaMap: JavaMap[A, B]): Map[A, B] =
    javaMap
      .asScala
      .toMap

  def asScala[A](javaList: JavaList[A]): List[A] =
    javaList
      .asScala
      .toList
