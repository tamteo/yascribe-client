package com.miriya.miri.scribe.client.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Defined the scribe properties.
 */
public class ScribeProperties {
    /**
     * the property key use to specify the path of the configuration file to load
     */
    public static final String CONFIG_FILE_PROPERTY = "com.miriya.miri.scribe.property-file";
    /**
     * the default configuration file name: scribe.config.properties
     */
    public static String DEFAULT_CONFIG_FILE = "scribe.config.properties";

    /**
     * the property key use to specify the project dir
     */
    public static final String PROJECT_DIR_PROPERTY = "com.miriya.miri.scribe.project-dir";

    /**
     * the property key use to specify the project dir
     */
    public static final String REPORT_DIR_PROPERTY = "com.miriya.miri.scribe.report-dir";

    /**
     * the scribe folder property
     */
    public static final String SCRIBE_FOLDER_PROPERTY = "com.miriya.miri.scribe.yascribe-dir";

    /**
     * the property key of the server name
     */
    public static final String SERVER_NAME_PROPERTY = "com.miriya.miri.scribe.server-name";

    /**
     * the property key of the server port
     */
    public static final String SERVER_PORT_PROPERTY = "com.miriya.miri.scribe.server-port";

    /**
     * the default server name: localhost
     */
    public static final String DEFAULT_SERVER_NAME = "localhost";

    /**
     * the default server port: 9090
     */
    public static final int DEFAULT_SERVER_PORT = 9090;

    /**
     * <pre>
     * the property key of the browser name :
     *  - Chrome
     *  - Firefox
     *  - Opera
     *  - Edge
     *  - IExplorer
     *  - Chromium
     *  - Safari
     * </pre>
     */
    public static final String BROWSER_NAME = "com.miriya.miri.scribe.stylus.browser-name";
    /**
     * the default browser name
     */
    public static final String DEFAULT_BROWSER_NAME = "Chrome";

    /**
     * Sets the specified properties to System if they are not already defined as system properties.
     * @param properties the properties
     */
    static void setProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            if (System.getProperty(key) == null) {
                System.setProperty(key, entry.getValue().toString());
            }
        }
    }

    /**
     * Loads the configuration properties from the default configuration file in the working dir.
     */
    static void loadConfigurationFromWorkingDir() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(DEFAULT_CONFIG_FILE));
            setProperties(properties);
        } catch (IOException e) {
            loadConfigurationFromResources();
        }
    }

    /**
     * Loads the configuration properties from the default configuration file in resources.
     */
    static void loadConfigurationFromResources() {
        InputStream stream = ScribeProperties.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
        Properties properties = new Properties();
        try {
            properties.load(stream);
            setProperties(properties);
        } catch (IOException e) {}
    }

    /**
     * Loads the configuration properties from the specified file.
     * @param file the configuration file
     */
    static void loadConfiguration(File file) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(file));
            setProperties(properties);
        } catch (IOException e) {
            loadConfigurationFromWorkingDir();
        }
    }

    /**
     * <pre>
     * Loads all properties from the configuration file:
     *  - load the configuration file defined by the property CONFIG_FILE_PROPERTY
     *  - if CONFIG_FILE_PROPERTY is not defined or the configuration is not found
     *  - then load the default configuration file (configuration.properties) from the working dir
     *  - if the default configuration file is not found
     *  - then load the default configuration file from resources
     * Once properties are loaded, sets each loaded property to System
     * if it is not already defined as a system property.
     * </pre>
     */
    public static void load() {
        String configFile = System.getProperty(CONFIG_FILE_PROPERTY);
        File file;
        if (configFile == null) {
            String projectDir = getProjectDir();
            file = projectDir == null ? new File(DEFAULT_CONFIG_FILE) : new File(projectDir, DEFAULT_CONFIG_FILE);
        } else {
            file = new File(configFile);
        }
        loadConfiguration(file);
    }

    /**
     * Gets the project dir.
     * @return the project dir
     */
    public static String getProjectDir() {
        return System.getProperty(PROJECT_DIR_PROPERTY, "./");
    }

    /**
     * Gets the name of the configured scribe folder.
     * @return the scribe folder name
     */
    public static String getScribeFolder() {
        return System.getProperty(SCRIBE_FOLDER_PROPERTY, getProjectDir());
    }

    /**
     * Gets the server name.
     * @return server name defined in properties, DEFAULT_SERVER_NAME if not defined
     */
    public static String getServerName() {
        return System.getProperty(SERVER_NAME_PROPERTY, DEFAULT_SERVER_NAME);
    }

    /**
     * Gets the server port.
     * @return server port defined in properties, DEFAULT_SERVER_PORT if not defined
     */
    public static int getServerPort() {
        int port = DEFAULT_SERVER_PORT;
        String value = System.getProperty(SERVER_PORT_PROPERTY);
        if (value != null) {
            try {
                port = Integer.parseInt(value);
            } catch (NumberFormatException e) {}
        }
        return port;
    }

    /**
     * Gets the name of the configured browser.
     * @return the browser name
     */
    public static String getBrowserName() {
        return System.getProperty(BROWSER_NAME, DEFAULT_BROWSER_NAME);
    }

    /**
     * Gets the report directory.
     * @return the report directory, default one is "PROJECT_DIR/target/scribe-reports"
     */
    public static String getReportDir() {
        return System.getProperty(REPORT_DIR_PROPERTY, getProjectDir() + "/target/scribe-reports");
    }
}
