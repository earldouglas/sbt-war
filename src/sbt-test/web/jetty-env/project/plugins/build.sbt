libraryDependencies <<= (libraryDependencies, appConfiguration) {
  (deps, app) =>
	deps :+ "com.github.siasia" %% "xsbt-web-plugin" % app.provider.id.version
}
