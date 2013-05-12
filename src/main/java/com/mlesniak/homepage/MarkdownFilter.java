package com.mlesniak.homepage;

import com.petebevin.markdown.MarkdownProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
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
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        path = rewritePath(path);

        handleCookies(req, (HttpServletResponse) response);

        try {
            File file = new File(filterConfig.getInitParameter("root") + path);
            if (file.exists() && file.isDirectory() == false) {
                createMarkdown(response, file);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO mlesniak Logging.
        }

        chain.doFilter(request, response);
    }

    private void handleCookies(HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = getCookie(request, "mlesniak.com");
        if (cookieValue == null) {
            String id = RandomStringUtils.random(40, true, true);
            Cookie cookie = new Cookie("mlesniak.com", id);
            response.addCookie(cookie);
        } else {
            // TODO
        }
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (StringUtils.equals(cookie.getName(), name)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void createMarkdown(ServletResponse response, File file) throws IOException {
        String header = FileUtils.readFileToString(new File(filterConfig.getInitParameter("root") + HEADER_HTML));
        String footer = FileUtils.readFileToString(new File(filterConfig.getInitParameter("root") + FOOTER_HTML));

        String output = null;
        if (file.getPath().endsWith(".md")) {
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
        if (isDir && path.endsWith("/") == false) {
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
