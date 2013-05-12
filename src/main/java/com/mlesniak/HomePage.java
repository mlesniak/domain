package com.mlesniak;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;

import java.util.Date;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;
    private BackgroundThreadManager btm;

    public HomePage(final PageParameters parameters) {
        super(parameters);
        add(new Label("message", new Model<String>() {
            @Override
            public String getObject() {
                return (new Date().toString()) + ": " + btm.getTick();
            }
        }));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        btm = MainApplication.getBackgroundThreadManager();

        CookieUtils cu = new CookieUtils();
        String cookie = cu.load("mlesniak.com");
        if (cookie == null) {
            String id = RandomStringUtils.random(40, true, true);
            cu.save("mlesniak.com", id);
        } else {
            // TODO
        }
    }
}
