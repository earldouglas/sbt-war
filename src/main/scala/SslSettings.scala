package com.earldouglas.xsbtwebplugin

case class SslSettings(
      val port : Int, 
      val keystore : String, 
      val password: String,
      val keyPassword : String) 
