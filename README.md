# Usage
Setup [SBT](http://github.com/harrah/xsbt/).

Add plugin to project in `project/plugins/build.sbt`:

    resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"    
		
    //Following means libraryDependencies += "com.github.siasia" %% "xsbt-web-plugin" % "0.1.1-<sbt version>""
    libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.1.1-"+v))

Artifacts are available for 0.10.1 SBT versions.

Inject plugin settings into project in `build.sbt`:

    seq(webSettings :_*)
		
Add other required stuff like `web.xml`, properties and source code.
		
This will add commands required to run web application. Invoke `jetty-run`(as in sbt 0.7) to run web application:

    > jetty-run
    [info] jetty-7.3.0.v20110203
    [info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
    [info] started o.e.j.w.WebAppContext{/,file:/home/siasia/work/sample-copy/target/webapp/},/home/siasia/work/sample-copy/target/webapp
    [info] Started SelectChannelConnector@0.0.0.0:8080
		
Web application is now accesible on [http://localhost:8080](http://localhost:8080)

Use `jetty-stop` to stop Jetty and `prepare-webapp` to build webapp after you've done some changes:

    > prepare-webapp
    [success] Total time: 1 s, completed 02.05.2011 17:08:43
    > [info] Reloading web application...
    17:08:44.595 [Scanner-0] DEBUG net.liftweb.http.LiftServlet - Destroyed Lift handler.
    [info] stopped o.e.j.w.WebAppContext{/,file:/home/siasia/work/sample-copy/target/webapp/},/home/siasia/work/sample-copy/target/webapp
    [info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
    [info] started o.e.j.w.WebAppContext{/,file:/home/siasia/work/sample-copy/target/webapp/},/home/siasia/work/sample-copy/target/webapp
    [info] Reload complete.
		
    > jetty-stop
    17:09:21.739 [main] DEBUG net.liftweb.http.LiftServlet - Destroyed Lift handler.
    [info] stopped o.e.j.w.WebAppContext{/,file:/home/siasia/work/sample-copy/target/webapp/},/home/siasia/work/sample-copy/target/webapp
		
## Running Lift

**Please note that Jetty dependencies should go to `jetty` configuration instead of `test` as it was in 0.7.x version.**

See [Using SBT](http://www.assembla.com/wiki/show/liftweb/Using_SBT) on the lift website for an example setup.
		
Also you can download [Lift Basic Sample](http://github.com/downloads/siasia/xsbt-web-plugin/lift-basic-xsbt.zip) to see an example project (albeit an old one).

