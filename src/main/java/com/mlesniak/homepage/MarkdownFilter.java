package com.mlesniak.homepage;

import com.petebevin.markdown.MarkdownProcessor;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * A special filter which checks if a file with the given name (and extension .md exists) and then returns the .html of
 * the .md in the response. Serves other files if they exist.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
public class MarkdownFilter implements Filter {
    public static final String HEADER_HTML = "header.html";
    public static final String FOOTER_HTML = "footer.html";
    public static final String COOKIE_NAME = "mlesniak.com";
    private FilterConfig filterConfig;

    @Inject
    VisitorLogDao dao;

    HttpServletRequest request;
    HttpServletResponse response;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        request = (HttpServletRequest) req;
        response = (HttpServletResponse) resp;

        String path = request.getRequestURI().substring(request.getContextPath().length());
        path = rewritePath(path);

        try {
            File file = new File(filterConfig.getInitParameter("root") + path);
            if (file.exists() && !file.isDirectory()) {
                createMarkdown(file);
                return;
            }
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }

        chain.doFilter(req, resp);
    }

    private void updateLog() {
        try {
            dao.updateOrCreateVisitorLog(request, response);
        } catch (Exception e) {
            System.out.println("Cookie handling failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createMarkdown(File file) throws IOException {
        String header = FileUtils.readFileToString(new File(filterConfig.getInitParameter("root") + HEADER_HTML));
        String footer = FileUtils.readFileToString(new File(filterConfig.getInitParameter("root") + FOOTER_HTML));

        String output = null;
        if (file.getPath().endsWith(".md")) {
            updateLog();

            MarkdownProcessor md = new MarkdownProcessor();
            String content = FileUtils.readFileToString(file);
            output = header + md.markdown(content) + footer;
            response.getOutputStream().print(output);
        } else {
            byte[] bout = FileUtils.readFileToByteArray(file);
            response.getOutputStream().write(bout);
        }

        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
    }

    private String rewritePath(String path) {
        boolean isDir = new File(filterConfig.getInitParameter("root") + path).isDirectory();
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
