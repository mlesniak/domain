package com.mlesniak.homepage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The DAO Manager for jobs for which CDI does not work. We define a special listener in web.xml such that the singleton
 * instance is initialized at startup.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
@Singleton
public class DaoManager implements ServletContextListener {
    private static DaoManager daoManager;
    @Inject
    private VisitorLogDao visitorLogDao;

    public static DaoManager get() {
        return daoManager;
    }

    public VisitorLogDao getVisitorLogDao() {
        return visitorLogDao;
    }

    @PostConstruct
    public void init() {
        daoManager = this;

    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // Empty.
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // Empty.
    }
}
