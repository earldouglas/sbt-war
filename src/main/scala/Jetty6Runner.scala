package com.earldouglas.xsbtwebplugin

                                
				import org.mortbay.jetty.{Server, Handler}
				import org.mortbay.jetty.handler.ContextHandlerCollection
				import org.mortbay.jetty.nio.SelectChannelConnector
				import org.mortbay.jetty.security.SslSocketConnector
				import org.mortbay.jetty.webapp.{WebAppClassLoader, WebAppContext, WebInfConfiguration, Configuration, JettyWebXmlConfiguration, TagLibConfiguration, WebXmlConfiguration}
				import org.mortbay.util.{Scanner => JScanner}
				import org.mortbay.log.{Log, Logger => JLogger}
				import org.mortbay.resource.ResourceCollection
				import org.mortbay.xml.XmlConfiguration
				import org.mortbay.jetty.plus.webapp.{EnvConfiguration, Configuration=>PlusConfiguration}
				

import sbt._
import classpath.ClasspathUtilities.toLoader
import scala.xml.NodeSeq

class Jetty6Runner extends Runner {
	private[this] val forceJettyLoad = classOf[Server]
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
			new JettyWebXmlConfiguration, 
			new TagLibConfiguration)
		context.setConfigurations(array)
	}
	private def deploy(contextPath: String, deployment: Deployment) = {
		import deployment._
		val context = new WebAppContext()
		context.setContextPath(contextPath)
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
	private def configureConnector(port: Int) {
		val conn = new SelectChannelConnector
		conn.setPort(port)
		server.addConnector(conn)
	}
        
	private def configureSecureConnector(ssl: SslSettings) {
		val conn = new SslSocketConnector()
		conn.setPort(ssl.port)
		conn.setKeystore(ssl.keystore)
		conn.setPassword(ssl.password)
		conn.setKeyPassword(ssl.keyPassword)
		server.addConnector(conn)    
	}
  
	def start(port: Int, ssl: Option[SslSettings], logger: AbstractLogger, apps: Seq[(String, Deployment)], customConf: Boolean, confFiles: Seq[File], confXml: NodeSeq) {
		if(server != null)
			return
		try { 
			Log.setLog(new DelegatingLogger(logger))
			server = new Server
			if(customConf)
				configureCustom(confFiles, confXml)
			else {
				configureConnector(port)
				ssl match {
				  case Some(s) => configureSecureConnector(s) 
				  case _ =>
				}
                                configureContexts(apps)
			}
			server.start()
		} catch {
			case e =>
				server = null
				throw e
		}
	}
	def reload(contextPath: String) {
		val (context, deployment) = contexts(contextPath)
		context.stop()
		setContextLoader(context, deployment.classpath)
		context.start()
	}
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
			def filesChanged(files: java.util.List[_]) { thunk() }
		}
		addListener(listener)
		start()
	}
}


