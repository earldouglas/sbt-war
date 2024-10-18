package com.earldouglas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Specifies server settings and components locations for running a webapp in-place. */
public class WebappComponentsConfiguration {

  /** The hostname to use for the server, e.g. "localhost" */
  public final String hostname;

  /** The port to use for the server, e.g. 8080 */
  public final int port;

  /**
   * The context path to use for the webapp.
   *
   * <p>For the root context path, use the empty string "".
   */
  public final String contextPath;

  /**
   * An empty directory that Tomcat requires to run. Represents the resources directory, but it can
   * be any empty directory.
   */
  public final File emptyWebappDir;

  /**
   * An empty directory that Tomcat requires to run. Represents the WEB-INF/classes directory, but
   * it can be any empty directory.
   */
  public final File emptyClassesDir;

  /**
   * The map of resources to serve.
   *
   * <p>The mapping is from source to destination, where:
   *
   * <ul>
   *   <li>source is the relative path within the webapp (e.g. index.html, WEB-INF/web.xml)
   *   <li>destination is the file on disk to serve
   * </ul>
   */
  public final Map<String, File> resourceMap;

  /**
   * Read configuration from a file at the specified location.
   *
   * @param configurationFilename the configuration filename to load
   * @throws IOException if something goes wrong
   * @return WebappComponentsConfiguration a loaded configuration
   */
  public static WebappComponentsConfiguration load(final String configurationFilename)
      throws IOException {
    return WebappComponentsConfiguration.load(new File(configurationFilename));
  }

  private static Map<String, File> parseResourceMap(final String raw) throws IOException {

    final Map<String, File> resourceMap = new HashMap<String, File>();

    final String[] rows = raw.split(",");

    for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      final String[] columns = rows[rowIndex].split("->");
      resourceMap.put(columns[0], new File(columns[1]));
    }

    return resourceMap;
  }

  /**
   * Read configuration from a file at the specified location.
   *
   * <p>The format of the file is a Properties file with the following fields:
   *
   * <ul>
   *   <li>hostname
   *   <li>port
   *   <li>contextPath
   *   <li>emptyWebappDir
   *   <li>emptyClassesDir
   *   <li>resourceMap
   * </ul>
   *
   * The resourceMap field is a list, concatenated by commas, of source/destination pairs, delimited
   * by -&gt;.
   *
   * <p>Example:
   *
   * <pre>
   * hostname=localhost
   * port=8989
   * contextPath=
   * emptyWebappDir=target/empty
   * emptyClassesDir=target/empty
   * resourceMap=bar.html-&gt;src/test/fakeproject/src/main/webapp/bar.html,\
   *             foo.html-&gt;src/test/fakeproject/src/main/webapp/foo.html,\
   *             baz/raz.css-&gt;src/test/fakeproject/src/main/webapp/baz/raz.css
   * </pre>
   *
   * @param configurationFile the configuration file to load
   * @throws IOException if something goes wrong
   * @return WebappComponentsConfiguration a loaded configuration
   */
  public static WebappComponentsConfiguration load(final File configurationFile)
      throws IOException {

    final InputStream inputStream = new FileInputStream(configurationFile);

    final Properties properties = new Properties();
    properties.load(inputStream);

    final Map<String, File> resourceMap = parseResourceMap(properties.getProperty("resourceMap"));

    return new WebappComponentsConfiguration(
        properties.getProperty("hostname"),
        Integer.parseInt(properties.getProperty("port")),
        properties.getProperty("contextPath"),
        new File(properties.getProperty("emptyWebappDir")),
        new File(properties.getProperty("emptyClassesDir")),
        resourceMap);
  }

  /**
   * Construct a new configuration from the given parameters.
   *
   * @param hostname the hostname to use for the server, e.g. "localhost"
   * @param port the port to use for the server, e.g. 8080
   * @param contextPath the context path to use for the webapp, e.g. ""
   * @param emptyWebappDir an empty directory that Tomcat requires to run
   * @param emptyClassesDir an empty directory that Tomcat requires to run
   * @param resourceMap the map of resources to serve
   */
  public WebappComponentsConfiguration(
      final String hostname,
      final int port,
      final String contextPath,
      final File emptyWebappDir,
      final File emptyClassesDir,
      final Map<String, File> resourceMap) {
    this.hostname = hostname;
    this.port = port;
    this.contextPath = contextPath;
    this.emptyWebappDir = emptyWebappDir;
    this.emptyClassesDir = emptyClassesDir;
    this.resourceMap = resourceMap;
  }
}
