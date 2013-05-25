package com.mlesniak.homepage;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Date;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;
    private BackgroundThreadManager btm;

    public HomePage(final PageParameters parameters) {
        super(parameters);
        add(new Label("message", new Model<String>() {
            @Override
            public String getObject() {
                return (new Date().toString());
            }
        }));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        btm = MainApplication.getBackgroundThreadManager();
    }
}

