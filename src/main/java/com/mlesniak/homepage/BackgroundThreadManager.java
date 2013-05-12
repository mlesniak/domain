package com.mlesniak.homepage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This class might become the controller for spawnable background processes which are started with the web application.
 * Currently it' simply a testbed.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class BackgroundThreadManager implements ServletContextListener {
    private int tick = 0;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(BackgroundThreadManager.class.toString(), this);

        // Start a simple daemon thread.
        Thread ticker = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(1000);
                        tick++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ticker.setDaemon(true);
        ticker.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    public int getTick() {
        return tick;
    }
}
