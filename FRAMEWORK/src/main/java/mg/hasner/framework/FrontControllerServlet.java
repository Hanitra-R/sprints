package mg.hasner.framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;
    private List<RouteMapping> routes;

    @Override
    public void init() throws ServletException {
        super.init();
        controllers = scanControllers();
        routes = scanRoutes(controllers);
        logRoutes(); // Pour le debug
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.POST);
    }

    // Support des autres méthodes si besoin
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.PUT);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.DELETE);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res, HttpMethod httpMethod)
            throws ServletException, IOException {
        String url = getRequestedUrl(req);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<h2>URL demandee : " + url + "</h2>");
        out.println("<h2>Methode HTTP : " + httpMethod + "</h2>");

        if ("/".equals(url)) {
            output(out);
            return;
        }

        UrlMethod urlMethod = new UrlMethod(url, httpMethod);
        RouteMapping route = findRoute(urlMethod);

        if (route == null) {
            // Afficher les URLs disponibles pour ce type de méthode
            out.println("<h3 style='color:red'>ERREUR 404 - Aucun mapping trouvé</h3>");
            out.println("<p>URL demandée : " + url + " (" + httpMethod + ")</p>");
            out.println("<p>URLs disponibles pour " + httpMethod + " : " + getExistingUrlsForMethod(httpMethod) + "</p>");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        out.println("<h3 style='color:green'>Mapping trouve</h3>");
        out.println("<p>Controller : <b>" + route.getControllerClass().getSimpleName() + "</b></p>");
        out.println("<p>Methode : <b>" + route.getMethod().getName() + "()</b></p>");
        
        // Indication sur l'invocation (pour le Sprint 04)
        out.println("<p style='color:blue'>➜ La méthode sera invoquée au Sprint 04</p>");
    }

    private void output(PrintWriter out) {
        out.println("<h3>Controleurs detectes</h3>");

        if (controllers == null || controllers.isEmpty()) {
            out.println("<p>Aucun controleur trouve.</p>");
            return;
        }

        out.println("<ul>");
        for (Class<?> clazz : controllers) {
            out.println("<li><b>" + clazz.getSimpleName() + "</b>");

            out.println("<ul>");
            boolean hasMappedMethod = false;
            for (RouteMapping route : routes) {
                if (route.getControllerClass().equals(clazz)) {
                    hasMappedMethod = true;
                    out.println("<li>" + route + "</li>");
                }
            }

            if (!hasMappedMethod) {
                out.println("<li>Aucune methode annotee @UrlMapping</li>");
            }

            out.println("</ul>");
            out.println("</li>");
        }
        out.println("</ul>");
        
        // Affichage du résumé des routes
        out.println("<hr/>");
        out.println("<h4>Resume des routes :</h4>");
        out.println("<ul>");
        for (RouteMapping route : routes) {
            out.println("<li>" + route + "</li>");
        }
        out.println("</ul>");
    }

    private List<Class<?>> scanControllers() {
        List<Class<?>> result = new ArrayList<>();
        File classesDir = getClassesDirectory();

        if (classesDir == null || !classesDir.exists()) {
            return result;
        }

        scanDirectory(classesDir, classesDir, result);
        return result;
    }

    private File getClassesDirectory() {
        String realPath = getServletContext().getRealPath("/WEB-INF/classes");

        if (realPath != null) {
            return new File(realPath);
        }

        try {
            URL classesUrl = Thread.currentThread().getContextClassLoader().getResource("");
            if (classesUrl != null) {
                return new File(classesUrl.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void scanDirectory(File rootDir, File currentDir, List<Class<?>> result) {
        File[] files = currentDir.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(rootDir, file, result);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                addControllerClass(rootDir, file, result);
            }
        }
    }

    private void addControllerClass(File rootDir, File classFile, List<Class<?>> result) {
        String className = toClassName(rootDir, classFile);

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = Class.forName(className, false, classLoader);
            if (clazz.isAnnotationPresent(Controller.class)) {
                result.add(clazz);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Ignore les classes non chargeables.
        }
    }

    private String toClassName(File rootDir, File classFile) {
        String rootPath = rootDir.getAbsolutePath();
        String classPath = classFile.getAbsolutePath();
        String relativePath = classPath.substring(rootPath.length() + 1);

        return relativePath
                .replace(File.separatorChar, '.')
                .replace(".class", "");
    }

    /**
     * SCAN DES ROUTES - MODIFICATION SPRINT 03
     * Prend en compte la méthode HTTP de l'annotation
     */
    private List<RouteMapping> scanRoutes(List<Class<?>> controllerClasses) {
        List<RouteMapping> result = new ArrayList<>();

        if (controllerClasses == null) {
            return result;
        }

        for (Class<?> clazz : controllerClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    String url = annotation.value();
                    HttpMethod httpMethod = annotation.method(); // Récupère la méthode HTTP
                    
                    // Vérification des doublons (même URL + même méthode HTTP)
                    UrlMethod urlMethod = new UrlMethod(url, httpMethod);
                    if (isDuplicate(result, urlMethod)) {
                        System.err.println("⚠️ DOUBLON détecté : " + urlMethod + " pour " + clazz.getSimpleName() + "." + method.getName());
                    }
                    
                    result.add(new RouteMapping(url, httpMethod, clazz, method));
                }
            }
        }

        return result;
    }
    
    /**
     * Vérifie si une route avec la même URL et la même méthode HTTP existe déjà
     */
    private boolean isDuplicate(List<RouteMapping> routes, UrlMethod urlMethod) {
        for (RouteMapping route : routes) {
            if (route.getUrlMethod().equals(urlMethod)) {
                return true;
            }
        }
        return false;
    }

    private String getRequestedUrl(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        return uri.isEmpty() ? "/" : uri;
    }

    /**
     * RECHERCHE DE ROUTE - MODIFICATION SPRINT 03
     * Utilise UrlMethod comme clé de recherche
     */
    private RouteMapping findRoute(UrlMethod urlMethod) {
        for (RouteMapping route : routes) {
            if (route.getUrlMethod().equals(urlMethod)) {
                return route;
            }
        }
        return null;
    }

    private String getExistingUrls() {
        if (routes == null || routes.isEmpty()) {
            return "aucune URL disponible";
        }

        List<String> urls = new ArrayList<>();
        for (RouteMapping route : routes) {
            urls.add(route.getUrlMethod().toString());
        }

        return String.join(", ", urls);
    }
    
    private String getExistingUrlsForMethod(HttpMethod method) {
        if (routes == null || routes.isEmpty()) {
            return "aucune URL disponible pour " + method;
        }

        List<String> urls = new ArrayList<>();
        for (RouteMapping route : routes) {
            if (route.getHttpMethod() == method) {
                urls.add(route.getUrl());
            }
        }

        return urls.isEmpty() ? "aucune" : String.join(", ", urls);
    }

    /**
     * LOG pour le debug dans la console du serveur
     */
    private void logRoutes() {
        System.out.println("=== ROUTES ENREGISTREES (Sprint 03) ===");
        if (routes == null || routes.isEmpty()) {
            System.out.println("Aucune route trouvee");
        } else {
            for (RouteMapping route : routes) {
                System.out.println("➜ " + route);
            }
        }
        System.out.println("=========================================");
    }
}