package com.mlesniak.homepage;

import com.mlesniak.homepage.config.Config;
import com.petebevin.markdown.MarkdownProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * A servlet which checks if a file with the given name (and extension .md exists) and then returns the .html of the .md
 * in the response. Serves other files if they exist.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class MarkdownFilter extends HttpServlet {
    public static final String HEADER_HTML = "header.html";
    public static final String FOOTER_HTML = "footer.html";
    public static final String COOKIE_NAME = "mlesniak.com";
    private static Logger log = LoggerFactory.getLogger(MarkdownFilter.class);
    Config config;
    @Inject
    private VisitorLogDao dao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MDC.put("session", request.getSession().getId());
        log.info("Request from " + request.getRemoteHost());
        config = Config.getConfig();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        path = rewritePath(path);

        try {
            File file = new File(config.get("root") + path);
            if (file.exists() && !file.isDirectory()) {
                createMarkdown(file, request, response);
                return;
            }
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    private void updateLog(HttpServletRequest request, HttpServletResponse response) {
        try {
            dao.updateOrCreateVisitorLog(request, response);
        } catch (Exception e) {
            System.out.println("Cookie handling failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void createMarkdown(File file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String header = FileUtils.readFileToString(new File(config.get("root") + HEADER_HTML));
        String footer = FileUtils.readFileToString(new File(config.get("root") + FOOTER_HTML));

        String output = null;
        if (file.getPath().endsWith(".md")) {
            log.debug("Parsing .md. file=" + file);
            updateLog(request, response);

            MarkdownProcessor md = new MarkdownProcessor();
            String content = FileUtils.readFileToString(file);
            output = header + md.markdown(content) + footer;
            response.getOutputStream().write(output.getBytes());
        } else {
            log.debug("Parsing other file. file=" + file);
            byte[] bout = FileUtils.readFileToByteArray(file);
            response.getOutputStream().write(bout);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String rewritePath(String path) {
        boolean isDir = new File(config.get("root") + path).isDirectory();
        if (isDir && !path.endsWith("/")) {
            path += "/";
        }

        if (path.endsWith("/")) {
            path += "index.md";
        } else if (path.endsWith(".html") && path.length() >= 5) {
            path = path.substring(1, path.length() - 5) + ".md";
        }
        return path;
    }

    @Override
    public void destroy() {
    }
}
