package com.earldouglas.sbt.war
import java.util.{List => JavaList}
import java.util.{Map => JavaMap}
import scala.collection.JavaConverters._

object TestCompat {

  def asScala[A, B](javaMap: JavaMap[A, B]): Map[A, B] =
    javaMap.asScala.toMap

  def asScala[A](javaList: JavaList[A]): List[A] =
    javaList.asScala.toList
}
