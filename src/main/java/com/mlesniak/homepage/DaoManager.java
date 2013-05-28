package com.mlesniak.homepage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Singleton
public class DaoManager {
    private static DaoManager daoManager;

    @Inject
    private VisitorLogDao visitorLogDao;

    public VisitorLogDao getVisitorLogDao() {
        return visitorLogDao;
    }

    @PostConstruct
    public void init() {
        daoManager = this;

    }

    public static DaoManager get() {
        return daoManager;
    }
}
