package com.mlesniak;

import com.mlesniak.homepage.HomePage;
import com.mlesniak.homepage.MainApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/** Simple test using the WicketTester */
public class TestHomePage {
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new MainApplication());
    }

    @Test
    public void homepageRendersSuccessfully() {
        //start and render the test page
        tester.startPage(HomePage.class);

        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
