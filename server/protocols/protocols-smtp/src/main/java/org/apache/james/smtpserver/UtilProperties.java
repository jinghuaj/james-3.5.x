package org.apache.james.smtpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UtilProperties {
    private static Map propsCache = new HashMap();
    private static Log logger = LogFactory.getLog(UtilProperties.class);

    public static String getProperty(String resourceName, String key,
                                     String defaultValue, Locale locale) {
        String prefix = resourceName
                .substring(0, resourceName.lastIndexOf("."));
        String after = resourceName.substring(
                resourceName.lastIndexOf(".") + 1, resourceName.length());
        String newResourceName = prefix + "_" + locale.getDisplayName() + "."
                + after;
        return getProperty(newResourceName, key, defaultValue);
    }

    public static String getProperty(String resourceName, String key,
                                     String defaultValue) {
        try {
            String value = getPropertyInFile(resourceName, key);
            if (value != null && !"".equalsIgnoreCase(value)) {
                return value;
            }
            return defaultValue;
        } catch (Exception e) {
            // logger.error(e);
            return defaultValue;
        }
    }

    private static String getPropertyInFile(String resourceName, String key)
            throws Exception {
        logger.debug("query file " + resourceName);
        Properties props = (Properties) propsCache.get(resourceName);
        UtilProperties utilProperties = new UtilProperties();
        if (props == null) {
            props = new Properties();
            ClassLoader loader = null;
            if (loader == null) {
                try {
                    loader = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    loader = utilProperties.getClass().getClassLoader();
                }
            }
            URL url = loader.getSystemResource(resourceName);
            InputStream in = null;

            try {
                logger.debug("start find url1:" + url.getPath());
                in = url.openStream();
                props.load(in);
                logger.debug("end find url1:" + url.getPath());
            } catch (Exception e) {
                try {
                    logger.debug("start find stream " + resourceName);
                    in = utilProperties.getClass().getResourceAsStream(
                            "/" + resourceName);
                    props.load(in);
                    logger.debug("end find stream " + resourceName);
                } catch (Exception e1) {
                    try {
                        logger.debug("failed to find stream " + resourceName);
                        File directory = new File(".");
                        String absPath = directory.getAbsolutePath();
                        logger.debug("current absolute path:" + absPath);
                        String path = absPath + File.separatorChar + ".." + File.separatorChar
                                + "conf" + File.separatorChar + resourceName;
                        logger.debug("path1:" + path);
                        File f = new File(path);
                        if (!f.exists()) {
                            logger.debug("can't find " + path);
                            path = absPath + File.separatorChar + "conf" + File.separatorChar
                                    + resourceName;
                            logger.debug("path2:" + path);
                            f = new File(path);
                            if (!f.exists()) {
                                logger.debug("can't find " + path);
                                throw e1;
                            }
                        }
                        logger.debug("find " + path);
                        props.load(new FileInputStream(path));
                    } catch (Exception e3) {
                        if (!(e3 instanceof NullPointerException)) {
                            logger.error(e3);
                        }
                        throw e3;
                    }
                }
            }
        }
        logger.debug("end query file " + resourceName);
        return props.getProperty(key);
    }

    public static String getProperty(String resourceName, String key) {
        return getProperty(resourceName, key, "");
    }

    private static String[] fileArray = new String[]{"runtime.properties",
            "sysconf.properties", "version.properties",
            "runtime.local.properties"};

    public static String getVariable(String key) {
        for (int i = 0; i < fileArray.length; i++) {
            String value = null;
            try {
                value = getPropertyInFile(fileArray[i], key);
                if (value != null && !"".equalsIgnoreCase(value)) {
                    return value;
                }
            } catch (Exception e) {
                // logger.error(e);
            }

        }
        return null;
    }

    public static String getVariable(String key, String defaultValue) {
        for (int i = 0; i < fileArray.length; i++) {
            String value = null;
            ;
            try {
                value = getPropertyInFile(fileArray[i], key);
                if (value != null && !"".equalsIgnoreCase(value)) {
                    return value;
                }
            } catch (Exception e) {
                if (!(e instanceof NullPointerException)) {
                    logger.error(e);
                }
            }

        }
        return defaultValue;
    }

    public static void main(String[] args) {
        String id = UtilProperties.getVariable("menu.npm.id");
        System.out.println("id:" + id);
    }
}