package com.mlesniak.homepage;

import com.mlesniak.homepage.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple servlet to allow for direct email sending by using a token over ssl. The token is defined in the configuration
 * file.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class EmailServlet extends HttpServlet {
    public static final String EMAIL_TOKEN = "emailToken";
    private static org.slf4j.Logger log = LoggerFactory.getLogger(EmailServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MDC.put("session", req.getSession().getId());
        log.info("EmailServlet called.");
        if (!ServletUtils.sslEnabled(req)) {
            log.warn("SSL not enabled. Not proceeding.");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String emailToken = Config.getConfig().get(EMAIL_TOKEN);
        if (StringUtils.isEmpty(emailToken)) {
            log.info("No token defined. Ignoring request");
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (req.getRequestURI().endsWith(emailToken)) {
            log.info("Authentication succeeded.");
            (new EmailJob()).sendEmail();
            resp.getOutputStream().println("EMail sent.");
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            log.warn("Authentication failed.");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
