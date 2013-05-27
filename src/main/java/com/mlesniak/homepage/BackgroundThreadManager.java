package com.mlesniak.homepage;

import org.apache.commons.lang3.StringUtils;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class might become the controller for spawnable background processes which are started with the web application.
 * Currently it's simply a testbed.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class BackgroundThreadManager implements ServletContextListener {
    @Inject
    BeanManager beanManager;
    private ServletContext servletContext;
    private Config config;

    public static BackgroundThreadManager get(HttpServletRequest request) {
        return (BackgroundThreadManager) request.getServletContext().getAttribute(BackgroundThreadManager.class.toString());
    }

    public String getAttribute(String param) {
        return servletContext.getInitParameter(param);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        config = Config.getConfig();
        servletContext = sce.getServletContext();
        servletContext.setAttribute(BackgroundThreadManager.class.toString(), this);

        for (Class<? extends BackgroundThread> clazz : collectStartingThreads()) {
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
            System.out.println("Starting thread: " + thread.getClass());
            thread.start();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends BackgroundThread>> collectStartingThreads() {
        List<Class<? extends BackgroundThread>> startingThreads = new ArrayList<Class<? extends BackgroundThread>>();

        List<String> jobs = config.getList("jobs");
        if (jobs == null) {
            return Collections.EMPTY_LIST;
        }
        for (String job : jobs) {
            if (StringUtils.isEmpty(job)) {
                continue;
            }

            Class<? extends BackgroundThread> clazz = null;
            try {
                clazz = (Class<? extends BackgroundThread>) this.getClass().getClassLoader().loadClass(job);
            } catch (Exception e) {
                System.out.println("Unable to load class. name=" + job);
                e.printStackTrace();
            }
            startingThreads.add(clazz);
        }

        return startingThreads;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
