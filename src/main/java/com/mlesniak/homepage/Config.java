package com.mlesniak.homepage;

import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

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
    private static Config singleton;
    private String configFilename;
    private Properties properties;
    private Scheduler scheduler;

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

    public Scheduler getScheduler() {
        return scheduler;
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

        StdSchedulerFactory factory = (StdSchedulerFactory) servletContextEvent.getServletContext().getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
        try {
            scheduler = factory.getScheduler();
            startInitialThreads();
        } catch (SchedulerException e) {
            System.out.println("Unable to get quartz scheduler.");
            e.printStackTrace();
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
