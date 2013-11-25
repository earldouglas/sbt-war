publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/JamesEarlDouglas/xsbt-web-plugin</url>
  <licenses>
    <license>
      <name>BSD 3-Clause</name>
      <url>https://github.com/JamesEarlDouglas/xsbt-web-plugin/blob/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:JamesEarlDouglas/xsbt-web-plugin.git</url>
    <connection>scm:git:git@github.com:JamesEarlDouglas/xsbt-web-plugin.git</connection>
  </scm>
  <developers>
    <developer>
      <id>siasia</id>
      <name>Artyom Olshevskiy</name>
      <url>http://github.com/siasia</url>
    </developer>
    <developer>
      <id>JamesEarlDouglas</id>
      <name>James Earl Douglas</name>
      <url>https://github.com/JamesEarlDouglas</url>
    </developer>
    <developer>
      <id>cdow</id>
      <name>cdow</name>
      <url>https://github.com/cdow</url>
    </developer>
  </developers>)
