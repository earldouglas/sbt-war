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
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.FileResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;

public class WarRunner {

  /**
   * Load configuration from the file in the first argument, and use it to start a new WarRunner.
   *
   * @param args the configuration filename to load and run
   * @throws Exception if something goes wrong
   */
  public static void main(final String[] args) throws Exception {

    final WarConfiguration warConfiguration = WarConfiguration.load(args[0]);

    final WarRunner warRunner =
        new WarRunner(warConfiguration);

    warRunner.start.run();
    warRunner.join.run();
  }

  /** A handle for starting the instance's server. */
  public final Runnable start;

  /** A handle for joining the instance's server. */
  public final Runnable join;

  /** A handle for stopping the instance's server. */
  public final Runnable stop;

  /**
   * Construct a new WarRunner using the given configuration.
   *
   * @param configuration the configuration to run
   * @throws IOException if something goes wrong
   */
  public WarRunner(final WarConfiguration configuration)
      throws IOException {

    final Tomcat tomcat = new Tomcat();
    tomcat.setPort(configuration.port);
    tomcat.setHostname(configuration.hostname);
    final File baseDir = new File(System.getProperty("user.dir") + "/target/tomcat." + configuration.port);
    new File(baseDir, "webapps").mkdirs();
    tomcat.setBaseDir(baseDir.getCanonicalPath());

    final Connector connector = new Connector();
    connector.setPort(configuration.port);
    tomcat.setConnector(connector);

    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println("warFile:");
    System.out.println(configuration.warFile.getAbsolutePath());
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println();

    final Context context =
        tomcat.addWebapp(configuration.contextPath, configuration.warFile.getAbsolutePath());
    ((StandardContext) context).setUnpackWAR(true);

    ((StandardJarScanFilter)
        ((StandardJarScanner) context.getJarScanner())
            .getJarScanFilter()
    ).setDefaultTldScan(false);

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
