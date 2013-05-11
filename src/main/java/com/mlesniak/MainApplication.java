package com.mlesniak;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Main application class.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class MainApplication extends WebApplication {
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();

        mountPage("app", HomePage.class);
    }
}
