package com.earldouglas.sbt.war

import org.apache.catalina.Context
import org.apache.catalina.WebResourceRoot
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.webresources.DirResourceSet
import org.apache.catalina.webresources.FileResourceSet
import org.apache.catalina.webresources.StandardRoot

import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

trait WebappComponentsRunner {
  def start(): Unit
  def join(): Unit
  def stop(): Unit
}

/** Launches a webapp composed of in-place resources, classes, and
  * libraries.
  *
  * emptyWebappDir and emptyClassesDir need to point to one or two empty
  * directories. They're not used to serve any content, but they are
  * required by Tomcat's internals.
  *
  * To use a root context path (i.e. /), set contextPath to the empty
  * string for some reason.
  */
object WebappComponentsRunner {

  def mkdir(file: File): File = {
    if (file.exists()) {
      if (!file.isDirectory()) {
        throw new FileAlreadyExistsException(file.getPath())
      } else {
        file
      }
    } else {
      val path: Path =
        FileSystems
          .getDefault()
          .getPath("target", "empty")
      try {
        Files.createDirectory(path)
        file
      } catch {
        case e: FileAlreadyExistsException =>
          file
      }
    }
  }

  def apply(
      hostname: String,
      port: Int,
      contextPath: String,
      emptyWebappDir: File,
      emptyClassesDir: File,
      resourceMap: Map[String, File]
  ): WebappComponentsRunner = {

    val tomcat: Tomcat = new Tomcat()
    tomcat.setHostname(hostname)

    val connector: Connector = new Connector()
    connector.setPort(port)
    tomcat.setConnector(connector)

    val context: Context =
      tomcat
        .addWebapp(
          contextPath,
          emptyWebappDir.getAbsolutePath()
        )

    // Set ClassLoader to prevent `ClassNotFoundException: ServletDef`
    // See https://coderanch.com/t/615403/application-servers/Starting-Web-App-Embeded-Tomcat
    context.setParentClassLoader(getClass().getClassLoader())

    val webResourceRoot: WebResourceRoot =
      new StandardRoot(context)

    webResourceRoot.addJarResources(
      new DirResourceSet(
        webResourceRoot,
        "/WEB-INF/classes",
        emptyClassesDir.getAbsolutePath(),
        "/"
      )
    )

    resourceMap
      .filter({ case (_, file) => file.exists() })
      .filter({ case (_, file) => file.isFile() })
      .foreach { case (path, file) =>
        webResourceRoot.addJarResources(
          new FileResourceSet(
            webResourceRoot,
            s"/${path}",
            file.getAbsolutePath(),
            "/"
          )
        )
      }

    context.setResources(webResourceRoot)

    new WebappComponentsRunner {
      override def start(): Unit = tomcat.start()
      override def join(): Unit = tomcat.getServer().await()
      override def stop(): Unit = {

        connector.stop()
        context.stop()
        tomcat.stop()

        connector.destroy()
        context.destroy()
        tomcat.destroy()
      }
    }
  }
}
