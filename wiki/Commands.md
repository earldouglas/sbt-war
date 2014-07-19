To start a web application, use `container:start`:

```
> container:start
[info] jetty-7.3.0.v20110203
[info] NO JSP Support for /, did not find org.apache.jasper.servlet.JspServlet
[info] started o.e.j.w.WebAppContext{/,[file:/home/siasia/projects/xsbt-web-plugin/src/main/webapp/]}
[info] Started SelectChannelConnector@0.0.0.0:8080
```

The application is now accesible on localhost, port 8080: [http://localhost:8080](http://localhost:8080)

To stop a running web application, use `container:stop`:

```
> container:stop
[info] stopped o.e.j.w.WebAppContext{/,[file:/home/siasia/projects/xsbt-web-plugin/src/main/webapp/]}
```
    
To reload a running web application, use `container:reload <context-path>`:

```
> container:reload /
```

To automatically reload a web application when source code is changed, use `~;container:start; container:reload <context-path>`:

```
> ~;container:start; container:reload /
```

To build a WAR package, use `package`.  To change the output directory of the WAR file, modify its `artifactPath`:

```scala
artifactPath in (Compile, packageWar) ~= { defaultPath =>
  file("dist") / defaultPath.getName
}
```