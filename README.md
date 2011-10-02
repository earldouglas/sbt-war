This is an attempt to rethink plugin architecture, clean out some mess and introduce flexibility along with separation of concerns.

# Changes summary

1. Existing plugin has been broken down into two separate facilities: container and webapp. Compound plugin is still available for simplicity
2. Webapp is running directly from sources without intermediate exploded war assembly
3. Breakdown into two parts allows to implement deployment of multiple projects into one web server
4. Web server runner interface is now little more abstract. This would allow to implement Tomcat support someday

# Usage

Setup [SBT](http://github.com/harrah/xsbt/).

Add plugin to project in `project/plugins/build.sbt`:

    resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"    
		
    addSbtPlugin("com.github.siasia" %% "xsbt-web-plugin" % "0.2-SNAPSHOT")
		
Artifacts are available for 0.11.0 SBT versions.

Inject plugin settings into project in `build.sbt`:

    seq(webSettings :_*)
		
or in case if you're using full configuration `webSettings` are `com.github.siasia.WebPlugin.webSettings`.

Add other required stuff like `web.xml`, properties and source code.

This will add commands required to run web application. Invoke `container:start` to run web application:

    > container:start
    [info] jetty-7.3.0.v20110203
    [info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
    [info] started o.e.j.w.WebAppContext{/,[file:/home/siasia/projects/xsbt-web-plugin/src/main/webapp/]}
    [info] Started SelectChannelConnector@0.0.0.0:8080

Web application is now accesible on [http://localhost:8080](http://localhost:8080)

Use `container:stop` to stop Jetty.

    > container:stop
    [info] stopped o.e.j.w.WebAppContext{/test1,[file:/home/siasia/projects/xsbt-web-plugin/sub1/src/main/webapp/]}
    [info] stopped o.e.j.w.WebAppContext{/test,[file:/home/siasia/projects/xsbt-web-plugin/sub/src/main/webapp/]}
		
Use `container:reload <context-path>` to reload corresponding webapp.

    > container:reload /test

## Running Lift

**Please note that Jetty dependencies should go to `container` configuration or configuration corresponding to your container instead of `test` as it was in 0.7.x version.**

Add Lift and Jetty to project dependencies:

    libraryDependencies ++= Seq(
      "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
      "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
      "ch.qos.logback" % "logback-classic" % "0.9.26"
    )
		
or in case if you want to use Jetty 7:

    libraryDependencies ++= Seq(
      "net.liftweb" %% "lift-webkit" % "2.3" % "compile",
      "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container",
      "ch.qos.logback" % "logback-classic" % "0.9.26"
    )
		
## Multicontext

Here is an example of how multiple webapps could be deployed to a single Jetty instance:

    import sbt._
    
    import com.github.siasia._
    import WebappPlugin.webappSettings
    import Keys._
    
    object WebBuild extends Build {
      lazy val container = Container("container")
      
      lazy val rootSettings = Seq(
        libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
      ) ++ container.settings ++ container.deploy(
        "/sub1" -> sub1,
        "/sub2" -> sub2
      )    
      
      lazy val sub1 = Project("sub1", file("sub1")) settings(webappSettings :_*)
      lazy val sub2 = Project("sub2", file("sub2")) settings(webappSettings :_*)
      lazy val root = Project("root", file(".")) settings(rootSettings :_*)
      override def projects = Seq(root, sub1, sub2)
    }
		
Let's play with it a little

    > container:start
    2011-10-02 07:49:53.917:INFO::Logging to STDERR via org.mortbay.log.StdErrLog
    [info] jetty-6.1.22
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [info] NO JSP Support for /sub2, did not find org.apache.jasper.servlet.JspServlet
    [info] Started SocketConnector@0.0.0.0:8080
    [success] Total time: 1 s, completed 02.10.2011 7:49:54
    > container:stop 
    [success] Total time: 0 s, completed 02.10.2011 7:54:34
    > container:start
    [info] jetty-6.1.22
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [info] NO JSP Support for /sub2, did not find org.apache.jasper.servlet.JspServlet
    [info] Started SocketConnector@0.0.0.0:8080
    [success] Total time: 0 s, completed 02.10.2011 7:54:38
    > container:reload /sub 
    /sub1   /sub2
    > container:reload /sub1
    [info] NO JSP Support for /sub1, did not find org.apache.jasper.servlet.JspServlet
    [success] Total time: 0 s, completed 02.10.2011 7:54:53
    >

# License
This software is distributed under modified 3-clause BSD license. See [LICENSE](https://github.com/siasia/xsbt-web-plugin/blob/master/LICENSE) for more information.
