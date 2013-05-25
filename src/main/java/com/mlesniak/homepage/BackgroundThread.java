package com.mlesniak.homepage;

import javax.enterprise.inject.spi.BeanManager;

/** @author Michael Lesniak (mail@mlesniak.com) */
public abstract class BackgroundThread extends Thread {
    private BackgroundThreadManager manager;

    public abstract void inject(BeanManager manager);

    public void setManager(BackgroundThreadManager manager) {
        this.manager = manager;
    }

    public String getAttribute(String name) {
        return manager.getAttribute(name);
    }
}
