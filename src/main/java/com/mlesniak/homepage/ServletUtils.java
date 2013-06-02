package com.mlesniak.homepage;

import javax.servlet.http.HttpServletRequest;

/** @author Michael Lesniak (mail@mlesniak.com) */
public class ServletUtils {
    public static boolean sslEnabled(HttpServletRequest request) {
        return (String) request.getAttribute("javax.servlet.request.ssl_session") != null;
    }
}
