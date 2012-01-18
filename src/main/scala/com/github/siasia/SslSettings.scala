package com.github.siasia

case class SslSettings(
			val port : Int, 
			val keystore : String, 
			val password: String,
			val keyPassword : String) 
