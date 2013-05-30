package com.mlesniak.homepage;

import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Configuration bean.
 * <p/>
 * Later configuration should be loaded from the database if possible and the bean is only used as the initialization
 * config.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class Config implements ServletContextListener {
    public static final String OVERWRITE_DATABASE = "overwriteDatabase";
    private static Config singleton;
    private String configFilename;
    private Properties properties;
    private Scheduler scheduler;
    // Although never used, we have to inject the DAO manager here such that it is initialized for later threads, e.g. jobs.
    @Inject
    private DaoManager manager;
    @Inject
    ConfigDao configDao;

    public static Config getConfig() {
        if (singleton == null) {
            throw new IllegalStateException("Config not yet initialized.");
        }

        return singleton;
    }

    /**
     * To allow for an easier configuration, values for a key a.b.c.d are searched in this order:
     * <pre>
     *     d
     *     c.d
     *     b.c.d
     *     a.b.c.d
     * </pre>
     * This allows for a clearly arranged configuration file, although the user is responsible for preventing
     * duplicates.
     *
     * @param key The key to look for.
     *
     * @return The property.
     */
    public String get(String key) {
        load();

        String[] packageParts = key.split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = packageParts.length - 1; i >= 0; --i) {
            sb.insert(0, packageParts[i]);

            String result = retrieveValue(sb.toString());
            if (result != null) {
                return result;
            }
            sb.insert(0, '.');
        }

        return null;
    }

    private String retrieveValue(String key) {
        String value = configDao.get(key);
        if (value != null) {
            return value;
        }

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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public boolean getBoolean(String key) {
        String s = get(key);
        if (s == null) {
            return false;
        }

        return Boolean.parseBoolean(s);
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

        handleDatabase();

        StdSchedulerFactory factory = (StdSchedulerFactory) servletContextEvent.getServletContext().getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
        try {
            scheduler = factory.getScheduler();
            startInitialThreads();
        } catch (SchedulerException e) {
            System.out.println("Unable to get quartz scheduler.");
            e.printStackTrace();
        }

    }

    private void handleDatabase() {
        if (!getBoolean(OVERWRITE_DATABASE)) {
            System.out.println("Ignoring configuration file.");
            configDao.delete(OVERWRITE_DATABASE);
            return;
        }

        System.out.println("Filling database from file.");
        for (Object key : properties.keySet()) {
            if (StringUtils.equals((CharSequence) key, OVERWRITE_DATABASE)) {
                continue;
            }
            String value = (String) properties.get(key);
            System.out.println("Setting key=" + key + " value=" + value);
            configDao.save((String) key, value);
        }

    }

    @SuppressWarnings("unchecked")
    private void startInitialThreads() {
        List<String> jobs = getList("jobs");
        if (jobs == null) {
            System.out.println("No jobs= definition found.");
            return;
        }

        for (String job : jobs) {
            if (StringUtils.isEmpty(job)) {
                continue;
            }

            String cronExpression = get(job + ".cron");
            if (cronExpression == null) {
                System.out.println("No cron= definition for job=" + job);
                continue;
            }

            try {
                Class<? extends Job> clazz = (Class<? extends Job>) this.getClass().getClassLoader().loadClass(job);
                JobDetail jobDetail = newJob(clazz)
                                        .withIdentity(job)
                                        .build();
                Trigger trigger = newTrigger()
                                    .withIdentity(job + ".trigger")
                                    .withSchedule(cronSchedule(cronExpression))
                                    .build();
                System.out.println("Scheduling job=" + job + " with cron=" + cronExpression);
                getScheduler().scheduleJob(jobDetail, trigger);
            } catch (Exception e) {
                System.out.println("Unable to create job. class=" + job);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            getScheduler().shutdown();
        } catch (SchedulerException e) {
            System.out.println("Unable to shut down scheduler.");
            e.printStackTrace();
        }
    }
}
