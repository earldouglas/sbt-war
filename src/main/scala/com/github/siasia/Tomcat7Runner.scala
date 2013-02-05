package com.github.siasia

import org.apache.catalina.startup.Tomcat
import scala.xml.NodeSeq
import sbt.AbstractLogger
import sbt.classpath.ClasspathUtilities.toLoader
import java.io.File
import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import java.net.URLClassLoader
import org.apache.catalina.loader.WebappLoader
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter
import java.util.logging.Level

class Tomcat7Runner extends Runner {
	private var tomcat: Option[Tomcat] = None
	private var contexts = Map[String, Context]()
	
	def start(port: Int, ssl: Option[SslSettings], logger: AbstractLogger, apps: Seq[(String, Deployment)], customConf: Boolean, confFiles: Seq[File], confXml: NodeSeq) {
		tomcat = tomcat.orElse {
			val newTomcat = new Tomcat
			
			// TODO logging
			
			if(customConf) {
				//TODO config files
			} else {
				newTomcat.setPort(port)
				ssl.foreach { sslSettings =>
					val connector = configureSecureConnector(sslSettings)
					newTomcat.getService.addConnector(connector)
				}
			
				
				//TODO configure contexts
				val newContexts = apps.map { case (contextPath, deployment) =>
					val context = newTomcat.addWebapp(contextPath, deployment.webappResources(0).getAbsolutePath)
					context.setReloadable(true)
					
					val webLoader = new WebappLoader(loader)
					deployment.classpath.foreach(file => webLoader.addRepository(file.toURI.toURL.toString))
					context.setLoader(webLoader)
	
					(contextPath, context)
				}.toMap
			
				contexts = newContexts
			}
			
			newTomcat.start();
			
			Option(newTomcat)
		}
	}

	//TODO fix reloading
	def reload(contextPath: String) {
		val context = contexts.get(contextPath)
		context.foreach { ctx =>
			ctx.reload
		}
	}

	def stop() {
		tomcat.foreach { oldTomcat =>
			oldTomcat.stop
			oldTomcat.destroy
		}
		contexts = Map()
		tomcat = None
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
}
