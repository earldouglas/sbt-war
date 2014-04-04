package com.earldouglas.xsbtwebplugin

import org.eclipse.jetty.server.{Server, Handler}
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector                
import org.eclipse.jetty.webapp.{WebAppClassLoader, WebAppContext, WebInfConfiguration, Configuration, FragmentConfiguration, JettyWebXmlConfiguration, WebXmlConfiguration}
import org.eclipse.jetty.util.{Scanner => JScanner}
import org.eclipse.jetty.util.log.{Log, Logger => JLogger}
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.xml.XmlConfiguration
import org.eclipse.jetty.plus.webapp.{EnvConfiguration, PlusConfiguration}
import sbt._
import classpath.ClasspathUtilities.toLoader
import scala.xml.NodeSeq
import java.net.InetSocketAddress

class Jetty7Runner extends Runner {

  private[this] val forceJettyLoad = classOf[Server]
  private[this] val forceJettyLoad2 = classOf[SelectChannelConnector]
  private[this] val forceJettyLoad3 = classOf[SslSelectChannelConnector]

  private var server: Server = null
  private var contexts: Map[String, (WebAppContext, Deployment)] = Map()

  private def setContextLoader(context: WebAppContext, classpath: Seq[File]) {
    val appLoader = toLoader(classpath, loader)
    context.setClassLoader(appLoader)
  }

  private def setEnvConfiguration(context: WebAppContext, file: File) {
    val config = new EnvConfiguration { setJettyEnvXml(file.toURI.toURL) }
    val array : Array[Configuration] = Array(
      new WebInfConfiguration, 
      new WebXmlConfiguration,  
      config, 
      new PlusConfiguration, 
      new JettyWebXmlConfiguration)
    context.setConfigurations(array)
  }

  private def deploy(contextPath: String, deployment: Deployment) = {
    import deployment._
    val context = new WebAppContext()
    context.setContextPath(contextPath)
    context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")
    context.setBaseResource(
      new ResourceCollection(
        webappResources.map(_.getPath).toArray
      ))
    setContextLoader(context, classpath)
    env.foreach(setEnvConfiguration(context, _))
    if(!scanDirectories.isEmpty)
      new Scanner(scanDirectories, scanInterval, () => reload(contextPath))
    contexts += contextPath -> (context, deployment)
    context
  }  

  private def configureContexts(apps: Seq[(String, Deployment)]) {
    val contexts = apps.map { case (contextPath, deployment) => deploy(contextPath, deployment) }
    val coll = new ContextHandlerCollection()
    coll.setHandlers(contexts.toArray)
    server.setHandler(coll)
  }  

  private def configureCustom(confFiles: Seq[File], confXml: NodeSeq) {
    confXml.foreach(x => new XmlConfiguration(x.toString) configure(server))
    confFiles.foreach(f => new XmlConfiguration(f.toURI.toURL) configure(server))
  }

  private def configureConnector(addr: InetSocketAddress) {
    val conn = new SelectChannelConnector
    conn.setHost(addr.getAddress.getHostAddress)
    conn.setPort(addr.getPort)
    server.addConnector(conn)
  }
        
  private def configureSecureConnector(ssl: SslSettings) {
    import org.eclipse.jetty.http.ssl.SslContextFactory
    val context = new SslContextFactory()
    context.setKeyStore(ssl.keystore)
    context.setKeyStorePassword(ssl.password)
    val conn = new SslSelectChannelConnector(context)
    conn.setHost(ssl.addr.getAddress.getHostAddress)
    conn.setPort(ssl.addr.getPort)
    server.addConnector(conn)    
  }
  
  def start(addr: InetSocketAddress, ssl: Option[SslSettings], logger: AbstractLogger,
            apps: Seq[(String, Deployment)], customConf: Boolean, confFiles: Seq[File], confXml: NodeSeq) {
    if(server != null)
      return
    try { 
      //Log.setLog(new DelegatingLogger(logger))
      server = new Server
      if(customConf)
        configureCustom(confFiles, confXml)
      else {
        configureConnector(addr)
        ssl match {
          case Some(s) => configureSecureConnector(s) 
          case _ =>
        }
        configureContexts(apps)
      }
      server.start()
    } catch {
      case t: Throwable =>
        server = null
        throw t
    }
  }

  def reload(contextPath: String) {
    val (context, deployment) = contexts(contextPath)
    context.stop()
    setContextLoader(context, deployment.classpath)
    context.start()
  }

  def join(): Unit = if (server != null) server.join()

  def stop() {
    if(server != null)
      server.stop()
    server = null
  }

  class DelegatingLogger(delegate: AbstractLogger) extends LoggerBase(delegate) with JLogger {
    def getLogger(name: String) = this
  }

  class Scanner(scanDirs: Seq[File], scanInterval: Int, thunk: () => Unit) extends JScanner {
    import scala.collection.JavaConversions._
    setScanDirs(scanDirs)
    setRecursive(true)
    setScanInterval(scanInterval)
    setReportExistingFilesOnStartup(false)
    val listener = new JScanner.BulkListener {
      def filesChanged(files: java.util.List[String]) { thunk() }
    }
    addListener(listener)
    start()
  }
}


