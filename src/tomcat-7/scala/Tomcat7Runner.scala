package com.earldouglas.xsbtwebplugin

import org.apache.catalina.startup.Tomcat
import scala.xml.NodeSeq
import sbt.AbstractLogger
import sbt.classpath.ClasspathUtilities.toLoader
import sbt.classpath.ClasspathUtilities.rootLoader
import java.io.File
import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import java.net.URLClassLoader
import org.apache.catalina.loader.WebappLoader
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter
import java.util.logging.Level
import java.net.URL
import java.net.MalformedURLException
import java.util.logging.LogManager
import java.util.logging.ConsoleHandler
import sbt.IO

class Tomcat7Runner extends Runner {

  private[this] val forceTomcatLoad = classOf[Tomcat]

  private var server: Option[Server] = None
  
  def start(port: Int, ssl: Option[SslSettings], logger: AbstractLogger, apps: Seq[(String, Deployment)], customConf: Boolean, confFiles: Seq[File], confXml: NodeSeq) {
    server = server.orElse {
      val tomcat = new Tomcat
      
      // Configure logging
      val rootLogger = LogManager.getLogManager.getLogger("")
      for(handler <- rootLogger.getHandlers) {
        val handlerClass = handler.getClass
        
        // Remove ConsoleHandler so we control the console and remove duplicate instances of our handler
        if(
            handlerClass == classOf[ConsoleHandler] || 
            handlerClass == classOf[DelegatingHandler]) {
          rootLogger.removeHandler(handler)
        }
      }
      
      rootLogger.addHandler(new DelegatingHandler(logger))

      // Configure tomcat
      val baseDir = IO.createTemporaryDirectory
      tomcat.setBaseDir(baseDir.getAbsolutePath)

      val contexts = if(customConf) {
        //TODO config files
        throw new RuntimeException("Tomcat does not currently support a custom conf")
      } else {
        tomcat.setPort(port)
        ssl.foreach { sslSettings =>
          val connector = configureSecureConnector(sslSettings)
          tomcat.getService.addConnector(connector)
        }
      
        createContexts(tomcat, apps)
      }
      
      tomcat.start();
      
      Option(Server(tomcat, contexts))
    }
  }

  def reload(contextPath: String) {
    server.foreach { case Server(tomcat, contexts) =>
      val context = contexts.get(contextPath)
      context.foreach( _.reload )
    }
  }

  def stop() {
    server.foreach { case Server(tomcat, contexts) =>
      tomcat.stop
      tomcat.destroy
    }

    server = None
  }
  
  private def configureSecureConnector(ssl: SslSettings): Connector = {
    val connector = new Connector()
    connector.setPort(ssl.port)
    connector.setSecure(true)
    connector.setScheme("https")
    connector.setAttribute("SSLEnabled", true)
    connector.setAttribute("keystorePass", ssl.password)
    connector.setAttribute("keystoreFile", ssl.keystore)
    connector.setAttribute("keyPass", ssl.keyPassword)
    
    connector
  }
  
  private def createContexts(newTomcat: Tomcat, apps: Seq[(String, Deployment)]): Map[String, Context] = {
    apps.map { case (contextPath, deployment) =>
      // Tomcat requires that we specify a single 'doc base' for each context.  We treat the first webappResource
      // as the doc base and add the others as 'watched resources'.  However, if no webappResources are specified
      // we need to blow up.
      if(deployment.webappResources.size < 1)
        throw new RuntimeException("There must be at least 1 webapp resource configured")
      
      val context = newTomcat.addWebapp(contextPath, deployment.webappResources.head.getAbsolutePath)
      deployment.webappResources.tail.foreach { file =>
        context.addWatchedResource(file.getAbsolutePath)
      }

      val webLoader = new ReloadableWebappLoader(loader)
      deployment.classpath.foreach(file => webLoader.addRepository(file.toURI.toURL.toString))
      context.setLoader(webLoader)

      (contextPath, context)
    }.toMap
  }
  
  private case class Server(tomcat: Tomcat, contexts: Map[String, Context])
  
  private class DelegatingHandler(delegate: AbstractLogger) extends Handler {
    val formatter = new SimpleFormatter
  
    // dummy methods
    def close {}
    def flush {}
    
    def publish(record: LogRecord) {
      val message = formatter.format(record)
      
      record.getLevel match {
        case Level.SEVERE => delegate.error(message)
        case Level.WARNING => delegate.warn(message)
        case Level.INFO => delegate.info(message)
        case _ => delegate.debug(message)
      }
    }
  }
  
  /**
   * Based on code from the Maven Tomcat plugin
   */
  private class ReloadableWebappLoader(loader: ClassLoader) extends WebappLoader(loader) {
    /**
     * Last modification times of all jar and class files.
     */
    private var modificationMap = Map[String, Long]()
    
    override def addRepository(repository: String) {
      super.addRepository(repository)
      try {
        val file = new File( new URL(repository).getPath.replaceAll("%20", " "))
        modificationMap = modificationMap ++ addFile(file)
      } catch {
        case muex: MalformedURLException => throw new RuntimeException(muex)
      }
    }
    
    /**
     * Check if {@link WebappLoader} says modified() and check files from added repositories.
     */
    override def modified: Boolean = {
      val superModified = super.modified
      
      val modifiedFiles = modificationMap.filter {case (path, lastModified) =>
        val file = new File(path)
        file.exists && file.lastModified > lastModified
      }
      
      val updatedFileMap = modifiedFiles.map {case (path, lastModified) =>
        val file = new File(path)
        addFile(file)
      }.flatten
      
      modificationMap = modificationMap ++ updatedFileMap
      
      superModified || !modifiedFiles.isEmpty
    }
    
    /**
     * Converts a file or a directory to a map containing the path to the file and its last modified timestamp
     */
    private def addFile(file: File): Map[String, Long] = {
      if(file.isDirectory()) {
        //remember also directory last modification time
        val fileModifiedMap = toModificationMap(file)
        val childFilesModified = file.listFiles.map(addFile _).flatten.toMap
        
        fileModifiedMap ++ childFilesModified
      } else if(file.isFile) {
        toModificationMap(file)
      } else { Map() }
    }
    
    /**
     * Converts a file to a map containing the path to the file and its last modified timestamp
     */
    private def toModificationMap(file: File) = Map(file.getAbsolutePath -> file.lastModified)
    
    /**
     * We replace the system class loader with the root loader.  This is
     * because the SBT libraries are on the system class loader.  This creates
     * conflicts for the Scala libraries.  Tomcat doesn't provide a way to do
     * this, so we have to hack it in by bypassing the private modifier.
     */
    override protected def startInternal(): Unit = {
      super.startInternal()
      
      val classLoader = getClassLoader()
      
      val systemLoaderField = classLoader.getClass.getDeclaredField("system")
      systemLoaderField.setAccessible(true)
      systemLoaderField.set(classLoader, rootLoader)
      systemLoaderField.setAccessible(false)
    }
  }
}
