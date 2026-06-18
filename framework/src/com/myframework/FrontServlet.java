package com.myframework;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.myframework.core.RouteRegistry;
import com.myframework.core.RouteRegistry.Route;
import com.myframework.demo.DemoController;

/**
 * Front controller servlet that catches every request (via web.xml mapping /*)
 * and simply displays information about the requested URL. This ensures there
 * is no 404: every URL is handled by this servlet.
 */
public class FrontServlet extends HttpServlet {

    private transient RouteRegistry registry;

    @Override
    public void init() throws ServletException {
        super.init();
        registry = new RouteRegistry();
        // Discover annotated routes from controllers here
        registry.discoverFrom(DemoController.class);
        // In the future, add more controllers with registry.discoverFrom(YourController.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);

        // Resolve logical path: URI without contextPath
        String uri = req.getRequestURI();
        String context = req.getContextPath() == null ? "" : req.getContextPath();
        String path = uri.startsWith(context) ? uri.substring(context.length()) : uri;
        if (path.isEmpty()) path = "/";

        // Try to find an annotated route
        Route route = registry.find(req.getMethod(), path);
        if (route != null) {
            try {
                RouteRegistry.invoke(route, req, resp);
                return;
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("text/plain;charset=UTF-8");
                e.printStackTrace(resp.getWriter());
                return;
            }
        }

        // No route found: show info and list of known routes
        resp.setContentType("text/plain;charset=UTF-8");
        StringBuilder fullUrl = new StringBuilder(req.getRequestURL());
        String query = req.getQueryString();
        if (query != null && !query.isEmpty()) {
            fullUrl.append('?').append(query);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println("FrontServlet a intercepté votre requête.");
            out.println("Méthode: " + req.getMethod());
            out.println("URL: " + fullUrl);
            out.println("Chemin demandé (request URI): " + req.getRequestURI());
            out.println("Contexte (context path): " + req.getContextPath());
            out.println("Servlet path: " + req.getServletPath());
            out.println("Path info: " + req.getPathInfo());
            out.println();
            out.println("Routes connues (method path):");
            for (Route r : registry.all()) {
                out.println(" - " + r.httpMethod + " " + r.path + " -> " + r.method.getDeclaringClass().getSimpleName() + "." + r.method.getName());
            }
        }
    }
}

