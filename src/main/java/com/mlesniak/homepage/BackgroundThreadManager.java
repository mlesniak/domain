package com.mlesniak.homepage;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

/**
 * This class might become the controller for spawnable background processes which are started with the web application.
 * Currently it's simply a testbed.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class BackgroundThreadManager implements ServletContextListener {
    @Inject
    Config config;
    @Inject
    BeanManager beanManager;
    private ServletContext servletContext;

    public static BackgroundThreadManager get(HttpServletRequest request) {
        return (BackgroundThreadManager) request.getServletContext().getAttribute(BackgroundThreadManager.class.toString());
    }

    public String getAttribute(String param) {
        return servletContext.getInitParameter(param);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
        servletContext.setAttribute(BackgroundThreadManager.class.toString(), this);

        for (Class<? extends BackgroundThread> clazz : config.getInitialBackgroundThreads()) {
            BackgroundThread thread = null;
            Exception exception = null;
            try {
                thread = clazz.newInstance();
            } catch (IllegalAccessException ex) {
                exception = ex;
            } catch (InstantiationException ex) {
                exception = ex;
            }

            if (exception != null) {
                System.out.println("Unable to create background thread: " + exception.getMessage());
                exception.printStackTrace();
                continue;
            }

            thread.setManager(this);
            thread.inject(beanManager);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
