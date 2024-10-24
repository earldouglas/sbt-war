package com.earldouglas;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.FileResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Launches a webapp composed of in-place resources, classes, and libraries.
 *
 * <p>emptyWebappDir and emptyClassesDir need to point to one or two empty directories. They're not
 * used to serve any content, but they are required by Tomcat's internals.
 *
 * <p>To use a root context path (i.e. /), set contextPath to the empty string for some reason.
 */
public class WebappComponentsRunner {

  private static File mkdir(final File file) throws IOException {
    if (file.exists()) {
      if (!file.isDirectory()) {
        throw new FileAlreadyExistsException(file.getPath());
      } else {
        return file;
      }
    } else {
      final Path path = FileSystems.getDefault().getPath(file.getPath());
      try {
        Files.createDirectory(path);
        return file;
      } catch (FileAlreadyExistsException e) {
        return file;
      }
    }
  }

  /**
   * Load configuration from the file in the first argument, and use it to start a new
   * WebappComponentsRunner.
   *
   * @param args the configuration filename to load and run
   * @throws IOException if something goes wrong
   */
  public static void main(final String[] args) throws IOException {

    final WebappComponentsConfiguration webappComponentsConfiguration =
        WebappComponentsConfiguration.load(args[0]);

    final WebappComponentsRunner webappComponentsRunner =
        new WebappComponentsRunner(webappComponentsConfiguration);

    webappComponentsRunner.start.run();
    webappComponentsRunner.join.run();
  }

  /** A handle for starting the instance's server. */
  public final Runnable start;

  /** A handle for joining the instance's server. */
  public final Runnable join;

  /** A handle for stopping the instance's server. */
  public final Runnable stop;

  /**
   * Construct a new WebappComponentsRunner using the given configuration.
   *
   * @param configuration the configuration to run
   * @throws IOException if something goes wrong
   */
  public WebappComponentsRunner(final WebappComponentsConfiguration configuration)
      throws IOException {

    mkdir(configuration.emptyWebappDir);
    mkdir(configuration.emptyClassesDir);

    final Tomcat tomcat = new Tomcat();
    tomcat.setHostname(configuration.hostname);

    final Connector connector = new Connector();
    connector.setPort(configuration.port);
    tomcat.setConnector(connector);

    final Context context =
        tomcat.addWebapp(configuration.contextPath, configuration.emptyWebappDir.getAbsolutePath());

    final WebResourceRoot webResourceRoot = new StandardRoot(context);

    webResourceRoot.addJarResources(
        new DirResourceSet(
            webResourceRoot,
            "/WEB-INF/classes",
            configuration.emptyClassesDir.getAbsolutePath(),
            "/"));

    configuration.resourceMap.forEach(
        (path, file) -> {
          if (file.exists() && file.isFile()) {
            webResourceRoot.addJarResources(
                new FileResourceSet(webResourceRoot, "/" + path, file.getAbsolutePath(), "/"));
          }
        });

    context.setResources(webResourceRoot);

    start =
        new Runnable() {
          @Override
          public void run() {
            try {
              tomcat.start();
            } catch (final LifecycleException e) {
              throw new RuntimeException(e);
            }
          }
        };

    join =
        new Runnable() {
          @Override
          public void run() {
            tomcat.getServer().await();
          }
        };

    stop =
        new Runnable() {
          @Override
          public void run() {
            try {

              connector.stop();
              context.stop();
              tomcat.stop();

              connector.destroy();
              context.destroy();
              tomcat.destroy();

            } catch (final LifecycleException e) {
              throw new RuntimeException(e);
            }
          }
        };
  }
}
