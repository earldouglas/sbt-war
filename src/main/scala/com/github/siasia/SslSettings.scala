package com.github.siasia

case class SslSettings(
			val port : Int, 
			val keystore : Option[String], 
			val password: Option[String],
			val keyPassword : Option[String]) 
