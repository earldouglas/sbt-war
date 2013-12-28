package com.earldouglas.xsbtwebplugin

import sbt._
import Keys._
import PluginKeys._
import Classpaths.managedJars
import Project.Initialize
import Scoped._
import sbinary.DefaultProtocol.StringFormat
import Cache.seqFormat
import complete._
import scala.xml.NodeSeq
import java.net.InetSocketAddress

case class Container(name: String) {

  def Configuration = config(name).hide
  def attribute = AttributeKey[Runner](name)
  def runner = attribute

  private implicit def attributeToRunner[Runner](key: AttributeKey[Runner])(implicit state: State):Runner =
    state.get(key).get

  private implicit def stateToRunner(state: State): Runner =
    state.get(attribute).get

  object Impl { 
    def eval[T](key: ScopedKey[sbt.Task[T]])(implicit state: State):T =
      EvaluateTask.processResult(
        Project.runTask(key, state) map (_._2) getOrElse
          sys.error("Error getting " + key),
        state.log)

    private implicit def keyToResult[T](key: TaskKey[T])(implicit state: State): T = eval(key)

    def newRunner(ref: ProjectRef, state: State) = {
      implicit val s = state
      val classpath = Attributed.data(fullClasspath in (ref, Configuration))
      state.put(attribute, Runner(classpath))
    }
  }

  def globalSettings = Seq(
    ivyConfigurations += Configuration
  )
  

  def containerSettings = Seq(
    managedClasspath <<= (classpathTypes, update) map {(ct, up) => managedJars(Configuration, ct, up)},
    fullClasspath <<= managedClasspath,
    onLoad in Global <<= (onLoad in Global, thisProjectRef) {
      (onLoad, containerProject) =>
        (state) =>
          Impl.newRunner(containerProject, onLoad(state))},
    onUnload in Global <<= (onUnload in Global) {
      (onUnload) =>
        (state) =>
          state.stop()
          onUnload(state)
    },
    host := "0.0.0.0",
    port := 8080,
    ssl := None,
    start <<= (state, host, port, ssl, apps, customConfiguration, configurationFiles, configurationXml) map {
      (state, host, port, ssl, apps, cc, cf, cx) => {
        val addr = new InetSocketAddress(host, port)
        state.start(addr, toSslSettings(ssl), state.log.asInstanceOf[AbstractLogger], apps, cc, cf, cx)
      }
    },
    discoveredContexts <<= apps map discoverContexts storeAs discoveredContexts triggeredBy start,
    reload <<= reloadTask(state),
    stop <<= (state) map { (state) => state.stop() },
    restart <<= (state, host, port, ssl, apps, customConfiguration, configurationFiles, configurationXml) map {
      (state, host, port, ssl, apps, cc, cf, cx) => {
        state.stop()
        val addr = new InetSocketAddress(host, port)
        state.start(addr, toSslSettings(ssl), state.log.asInstanceOf[AbstractLogger], apps, cc, cf, cx)
      }
    },
    customConfiguration := false,
    configurationFiles := Seq(),
    configurationXml := NodeSeq.Empty
  )

  def settings = globalSettings ++ inConfig(Configuration)(containerSettings)

  def pairToTask(conf: Configuration)(p: (String, ProjectReference)): Def.Initialize[Task[(String, Deployment)]] =
    (deployment in (p._2, conf)) map { (d) => (p._1, d) }

  type SettingSeq = Seq[Setting[_]]

  def deploy(map: (String, ProjectReference)*): SettingSeq =
    deploy(DefaultConf)(map :_*)

  def deploy(conf: Configuration)(map: (String, ProjectReference)*): SettingSeq =
    settings ++ inConfig(Configuration)(Seq(
      apps <<= map.map(pairToTask(conf)).join
    ))

  def toSslSettings(sslConfig: Option[(String,Int,String,String,String)]): Option[SslSettings] = {
    sslConfig.map { case (sslHost, sslPort, keystore, password, keyPassword) =>
      SslSettings(new InetSocketAddress(sslHost, sslPort), keystore, password, keyPassword)
    }
  }
  def discoverContexts(apps: Seq[(String, Deployment)]) = apps.map(_._1)
  
  def reloadParser: (State, Option[Seq[String]]) => Parser[String] =
  {
    import DefaultParsers._
    (state, contexts) => Space ~> token(NotSpace examples contexts.getOrElse(Seq.empty).toSet)
  }
  
  def reloadTask(state: TaskKey[State]): Def.Initialize[InputTask[Unit]] = {
    InputTask(loadForParser(discoveredContexts)(reloadParser)) { result =>
        (state, result) map { (state, context) => state.reload(context) }
    }
  }
}
