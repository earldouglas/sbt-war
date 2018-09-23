package com.earldouglas.xwp

import java.util.Date
import sbt.Keys._
import sbt._
import scala.io.Source

object WebXmlPlugin extends AutoPlugin {

  object autoImport {

    case class WebXmlListener(className: String) {
      override def toString: String =
        s"""|  <listener>
            |    <listener-class>${className}</listener-class>
            |  </listener>
            |""".stripMargin
    }

    case class WebXmlServlet( className: String
                            , mapping: String
                            ) {
      override def toString: String =
        s"""|  <servlet>
            |    <servlet-name>${className}</servlet-name>
            |    <servlet-class>${className}</servlet-class>
            |  </servlet>
            |  <servlet-mapping>
            |    <servlet-name>${className}</servlet-name>
            |    <url-pattern>${mapping}</url-pattern>
            |  </servlet-mapping>
            |""".stripMargin
    }

    case class WebXmlFilter( className: String
                           , mapping: String
                           ) {
      override def toString: String =
        s"""|  <filter>
            |    <filter-name>${className}</filter-name>
            |    <filter-class>${className}</filter-class>
            |  </filter>
            |  <filter-mapping>
            |    <filter-name>${className}</filter-name>
            |    <url-pattern>${mapping}</url-pattern>
            |  </filter-mapping>
            |""".stripMargin
    }

    lazy val webXmlListeners = settingKey[List[WebXmlListener]]("listeners for the generated deployment descriptor")
    lazy val webXmlServlets = settingKey[List[WebXmlServlet]]("servlets for the generated deployment descriptor")
    lazy val webXmlFilters = settingKey[List[WebXmlFilter]]("filter for the generated deployment descriptor")

  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override val projectSettings: Seq[Def.Setting[_]] =
    Seq( webXmlListeners := Nil
       , webXmlServlets := Nil
       , webXmlFilters := Nil
       ) ++
    inConfig(Compile) {
      resourceGenerators +=
        Def.task({
          val elements =
            webXmlListeners.value ++
            webXmlServlets.value ++
            webXmlFilters.value
          val webappTarget = (target in WebappPlugin.autoImport.webappPrepare).value
          if (elements.isEmpty) {
            Seq.empty
          } else {
            val webXml = webappTarget / "WEB-INF" / "web.xml"
            IO.write( webXml
                    , List( "<web-app>"
                          , ""
                          , elements.mkString("\n")
                          , "</web-app>"
                          ).mkString("\n")
                    )
            Seq(webXml)
          }
        }).taskValue
    }

}
