package com.mlesniak.homepage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Main application class.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class MainApplication extends WebApplication {
    private BackgroundThreadManager btm;

    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        btm = (BackgroundThreadManager) getServletContext().getAttribute(BackgroundThreadManager.class.toString());
        mountPage("app", HomePage.class);
    }

    public static BackgroundThreadManager getBackgroundThreadManager() {
        return ((MainApplication) WebApplication.get()).btm;
    }

}
