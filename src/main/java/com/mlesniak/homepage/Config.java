package com.mlesniak.homepage;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration bean.
 * <p/>
 * Later configuration should be loaded from the database if possible and the bean is only used as the initialization
 * config.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
@Stateless
public class Config {
    public int delayBetweenEmailsInMinutes() {
        return 12 * 60;
    }

    /** Return a list of initially started background jobs. */
    public List<Class<? extends BackgroundThread>> getInitialBackgroundThreads() {
        ArrayList<Class<? extends BackgroundThread>> classes = new ArrayList<Class<? extends BackgroundThread>>();
        classes.add(EmailJob.class);
        return classes;
    }
}
