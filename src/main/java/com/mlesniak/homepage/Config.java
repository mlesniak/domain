package com.mlesniak.homepage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration bean.
 * <p/>
 * Later configuration should be loaded from the database if possible and the bean is only used as the initialization
 * config.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class Config implements ServletContextListener {
    private static Config singleton;
    private String configFilename;
    private Properties properties;

    public static Config getConfig() {
        if (singleton == null) {
            throw new IllegalStateException("Config not yet initialized.");
        }

        return singleton;
    }

    public String get(String key) {
        load();

        return properties.getProperty(key);
    }

    public List<String> getList(String key) {
        List<String> list = new ArrayList<String>();

        String value = get(key);
        if (value != null) {
            for (String elem : value.split(",")) {
                list.add(elem.trim());
            }
        }

        return list;
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    private void load() {
        if (properties != null) {
            return;
        }

        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(configFilename)));
        } catch (IOException e) {
            System.out.println("Unable to load config file. name=" + configFilename);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (singleton != null) {
            return;
        }

        configFilename = servletContextEvent.getServletContext().getInitParameter("config-filename");
        load();
        singleton = this;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
