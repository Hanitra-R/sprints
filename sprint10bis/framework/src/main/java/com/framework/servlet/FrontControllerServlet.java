package com.framework.servlet;

import com.framework.annotation.APIREST;
import com.framework.annotation.Auth;
import com.framework.annotation.HttpMethod;
import com.framework.listener.FrontControllerListener;
import com.framework.model.MapFile;
import com.framework.model.MaSession;
import com.framework.model.ModelAndView;
import com.framework.util.ClasseUtilitaire;
import com.framework.annotation.Param;
import com.framework.util.JsonUtil;
import com.framework.util.ObjectBinder;
import com.framework.util.ParamUtil;
import com.framework.util.Mapping;
import com.framework.util.UploadUtil;
import com.framework.util.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class FrontControllerServlet extends HttpServlet {

    private String controllerPackage;
    private Map<String, String> controllers = new TreeMap<>();
    private Map<UrlMethod, Mapping> routes = new TreeMap<>();
    /** Sprint 10 : nom de l'attribut de session qui contient l'utilisateur connecte (lu dans le contexte). */
    private String sessionUserName;
    /** Sprint 10 bis : nom de l'attribut de session qui contient le ou les roles de l'utilisateur (lu dans le contexte). */
    private String sessionRoleName;

    /** Les controllers et les routes sont deja scannes par le FrontControllerListener. */
    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        controllerPackage = (String) context.getAttribute(FrontControllerListener.ATTR_PACKAGE);
        controllers = (Map<String, String>) context.getAttribute(FrontControllerListener.ATTR_CONTROLLERS);
        routes = (Map<UrlMethod, Mapping>) context.getAttribute(FrontControllerListener.ATTR_ROUTES);
        sessionUserName = (String) context.getAttribute(FrontControllerListener.ATTR_SESSION_USER);
        sessionRoleName = (String) context.getAttribute(FrontControllerListener.ATTR_SESSION_ROLE);

        if (controllers == null || routes == null) {
            throw new ServletException("Routes absentes du contexte : declarer "
                    + "com.framework.listener.FrontControllerListener dans web.xml");
        }
        log("Routes recuperees depuis le contexte : " + routes.size());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, HttpMethod.POST);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8"); // accents des champs de formulaire (POST)
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        String url = ClasseUtilitaire.normalizeUrl(path);

        // Accueil : liste de tous les controllers et de leurs routes
        if (url.isEmpty()) {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>FrontControllerServlet</h1>");
            out.println("<h2>Controllers et routes disponibles</h2>");
            out.println(listAll(req.getContextPath()));
            out.flush();
            return;
        }

        UrlMethod wanted = new UrlMethod(url, httpMethod);
        Mapping route = routes.get(wanted);
        if (route == null) {
            throw new ServletException("Aucune methode pour " + wanted + "." + otherMethodsHint(url, httpMethod)
                    + " Routes disponibles : " + describeRoutes());
        }

        // Sprint 10 / 10 bis : @Auth -> authentification (et roles) exigees avant d'executer la methode du controller
        if (!checkAuthorization(route, req, resp)) {
            return; // la reponse 401 ou 403 est deja envoyee
        }

        // Sprint 6 : methode annotee @APIREST -> JSON, sans requestDispatcher
        APIREST apiRest = route.getMethod().getAnnotation(APIREST.class);
        if (apiRest != null) {
            Object value = invoke(route, req);
            String json = (apiRest.alreadyJson() && value instanceof String)
                    ? (String) value
                    : JsonUtil.toJson(value);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter jsonOut = resp.getWriter();
            jsonOut.print(json);
            jsonOut.flush();
            return;
        }

        // Sinon : ancienne execution (ModelAndView + dispatcher)
        Object result = invoke(route, req);

        if (result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) result;
            for (Map.Entry<String, Object> attr : mv.getData().entrySet()) {
                req.setAttribute(attr.getKey(), attr.getValue());
            }
            req.getRequestDispatcher(mv.getViewPath()).forward(req, resp);
            return;
        }

        throw new ServletException("Type de retour invalide pour " + route.label()
                + " : ModelAndView attendu, trouve " + (result == null ? "null" : result.getClass().getName()));
    }

    /** Invoque par reflexion la methode du controller, avec les arguments pris dans la requete (Sprint 7). */
    private Object invoke(Mapping route, HttpServletRequest req) throws ServletException {
        Method method = route.getMethod();
        Object[] args = buildArguments(route, req);
        try {
            Object controller = route.getControllerClass().getDeclaredConstructor().newInstance();
            Object result = method.invoke(controller, args);
            flushSessions(args, req);
            return result;
        } catch (InvocationTargetException e) {
            throw new ServletException("Erreur dans " + route.label() + " : " + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new ServletException("Impossible d'invoquer " + route.label(), e);
        }
    }

    /**
     * Sprint 7 : un argument nomme "nom" recoit le parametre de requete "nom" (champ de formulaire
     * ou ?nom=... dans l'URL), converti vers le type de l'argument.
     * Sprint 7bis : @Param("nom") choisit le parametre a lire ; un argument de type objet est cree
     * et ses attributs sont remplis avec les parametres du meme nom.
     * Sprint 8 : un argument MapFile (ou byte[]) recoit le fichier du champ du meme nom, un argument
     * Map<String, MapFile> (ou Map<String, byte[]>) recoit tous les fichiers du formulaire.
     * Sprint 9 : un argument MaSession recoit une copie de la session HTTP, puis ses changements
     * sont recopies vers HttpSession apres l'invocation.
     */
    private Object[] buildArguments(Mapping route, HttpServletRequest req) throws ServletException {
        Parameter[] parameters = route.getMethod().getParameters();
        Object[] args = new Object[parameters.length];
        int objectCount = 0; // plusieurs objets : le prefixe (employe.nom) devient obligatoire
        for (Parameter q : parameters) {
            if (q.getType() != MaSession.class
                    && !ParamUtil.isSimple(q.getType()) && !ObjectBinder.isMultiple(q.getType())
                    && !ObjectBinder.isFileType(q.getType())) {
                objectCount++;
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            try {
                Param param = p.getAnnotation(Param.class);
                Class<?> type = p.getType();
                if (type == MaSession.class) {
                    args[i] = MaSession.from(req.getSession(true));
                    continue;
                }
                if (ObjectBinder.isFileType(type)) {
                    // Sprint 8 : fichiers envoyes par un formulaire multipart
                    if (type == java.util.Map.class || java.util.Map.class.isAssignableFrom(type)) {
                        args[i] = UploadUtil.fileMap(req, type, p.getParameterizedType()); // tous les fichiers
                    } else {
                        String field = (param != null) ? param.value() : (p.isNamePresent() ? p.getName() : "");
                        if (field.isEmpty()) {
                            throw new IllegalArgumentException("nom d'argument illisible pour un fichier : utiliser "
                                    + "@Param(\"...\") ou compiler l'application avec javac -parameters");
                        }
                        args[i] = (type == MapFile.class) ? UploadUtil.file(req, field) : UploadUtil.bytes(req, field);
                    }
                    continue;
                }
                if (!ParamUtil.isSimple(p.getType())) {
                    // 7bis : objet (avec objets imbriques), List / Set / tableau, ou plusieurs objets.
                    // Le nom de l'argument (ou @Param) sert de prefixe : employe.nom, employes[0].nom...
                    String name = (param != null) ? param.value() : (p.isNamePresent() ? p.getName() : "");
                    args[i] = ObjectBinder.isMultiple(p.getType())
                            ? ObjectBinder.bindMultiple(p.getParameterizedType(), p.getType(), name, req)
                            : ObjectBinder.bind(p.getType(), name, req, objectCount > 1);
                    continue;
                }
                if (param == null && !p.isNamePresent()) {
                    throw new ServletException("Nom d'argument illisible pour " + route.label()
                            + " : utiliser @Param(\"...\") ou compiler l'application avec javac -parameters");
                }
                String name = (param != null) ? param.value() : p.getName();
                args[i] = ParamUtil.convert(req.getParameter(name), p.getType());
            } catch (IllegalArgumentException e) {
                throw new ServletException("Argument " + (i + 1) + " de " + route.label() + " : " + e.getMessage(), e);
            }
        }
        return args;
    }

    /** Applique les copies MaSession a la session de la requete apres l'action du controleur. */
    private void flushSessions(Object[] args, HttpServletRequest req) throws ServletException {
        jakarta.servlet.http.HttpSession httpSession = req.getSession(false);
        for (Object arg : args) {
            if (arg instanceof MaSession) {
                if (httpSession == null) {
                    throw new ServletException("La session HTTP a disparu avant la synchronisation de MaSession");
                }
                ((MaSession) arg).flushTo(httpSession);
            }
        }
    }

    /**
     * Sprint 10 : verifie l'authentification avant d'executer la methode du controller.
     * Sprint 10 bis : verifie aussi les roles exiges par @Auth(roles = ...).
     * Retourne true si la requete peut continuer, false si la reponse 401 ou 403 est deja envoyee.
     * L'annotation @Auth de la methode est prioritaire sur celle de la classe ; sans @Auth la route est publique.
     */
    private boolean checkAuthorization(Mapping route, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Auth auth = route.getMethod().getAnnotation(Auth.class);
        if (auth == null) {
            auth = route.getControllerClass().getAnnotation(Auth.class);
        }
        if (auth == null) {
            return true;
        }
        if (sessionUserName == null) {
            throw new ServletException("Route " + route.label() + " annotee @Auth : context-param "
                    + FrontControllerListener.PARAM_SESSION_USER + " absent de web.xml");
        }
        HttpSession session = req.getSession(false); // false : une requete anonyme ne cree pas de session
        if (session == null || session.getAttribute(sessionUserName) == null) {
            handleUnauthorized(route, req, resp);
            return false;
        }
        // Sprint 10 bis : aucun role exige -> l'authentification suffit
        String[] required = auth.roles();
        if (required.length == 0) {
            return true;
        }
        if (sessionRoleName == null) {
            throw new ServletException("Route " + route.label() + " annotee @Auth(roles = ...) : context-param "
                    + FrontControllerListener.PARAM_SESSION_ROLE + " absent de web.xml");
        }
        if (hasRequiredRole(session.getAttribute(sessionRoleName), required)) {
            return true;
        }
        log("403 " + route.getUrlMethod() + " : roles exiges = " + String.join(", ", required));
        handleForbidden(route, req, resp);
        return false;
    }

    /** Sprint 10 : reponse 401 (session inexistante ou utilisateur non authentifie), en JSON ou en HTML selon la requete. */
    private void handleUnauthorized(Mapping route, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeError(route, req, resp, HttpServletResponse.SC_UNAUTHORIZED, "Non authentifie",
                "Authentification requise pour acceder a cette ressource.");
    }

    /**
     * Sprint 10 bis : le role de la session peut etre un String, une Collection (liste...) ou un tableau ;
     * il suffit qu'un des roles de la session soit l'un des roles exiges par @Auth.
     */
    private boolean hasRequiredRole(Object sessionRoles, String[] required) {
        if (sessionRoles == null) {
            return false;
        }
        if (sessionRoles instanceof Collection) {
            for (Object role : (Collection<?>) sessionRoles) {
                if (matches(role, required)) {
                    return true;
                }
            }
            return false;
        }
        if (sessionRoles.getClass().isArray()) {
            int length = Array.getLength(sessionRoles);
            for (int i = 0; i < length; i++) {
                if (matches(Array.get(sessionRoles, i), required)) {
                    return true;
                }
            }
            return false;
        }
        return matches(sessionRoles, required);
    }

    private boolean matches(Object role, String[] required) {
        if (role == null) {
            return false;
        }
        String value = String.valueOf(role);
        for (String wanted : required) {
            if (wanted.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /** Sprint 10 bis : reponse 403 (utilisateur authentifie mais sans le role requis), en JSON ou en HTML selon la requete. */
    private void handleForbidden(Mapping route, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeError(route, req, resp, HttpServletResponse.SC_FORBIDDEN, "Acces refuse",
                "Votre role ne permet pas d'acceder a cette ressource.");
    }

    /** Erreur en JSON si la route est @APIREST ou si le client demande application/json (sans text/html), sinon en HTML. */
    private void writeError(Mapping route, HttpServletRequest req, HttpServletResponse resp,
                            int status, String title, String message) throws IOException {
        String accept = req.getHeader("Accept");
        boolean json = route.getMethod().isAnnotationPresent(APIREST.class)
                || (accept != null && accept.contains("application/json") && !accept.contains("text/html"));
        PrintWriter out = resp.getWriter();
        if (json) {
            resp.setContentType("application/json;charset=UTF-8");
            out.print("{\"status\":" + status + ",\"error\":\"" + jsonEsc(title)
                    + "\",\"message\":\"" + jsonEsc(message) + "\"}");
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            out.println("<h1>" + status + " - " + esc(title) + "</h1>");
            out.println("<p>" + esc(message) + "</p>");
            out.println("<p>Route : " + esc(route.getUrlMethod().toString()) + "</p>");
        }
        out.flush();
    }

    /** Si l'URL existe avec une autre methode HTTP, le signale. */
    private String otherMethodsHint(String url, HttpMethod requested) {
        StringBuilder sb = new StringBuilder();
        for (HttpMethod other : HttpMethod.values()) {
            if (other != requested && routes.containsKey(new UrlMethod(url, other))) {
                sb.append(" Cette URL existe en ").append(other).append('.');
            }
        }
        return sb.toString();
    }

    /** Liste HTML : chaque controller avec ses routes (lien pour GET, bouton pour POST). */
    private String listAll(String contextPath) {
        if (controllers.isEmpty()) {
            return "<p>Aucun controller trouve dans " + esc(String.valueOf(controllerPackage)) + ".</p>";
        }
        StringBuilder sb = new StringBuilder("<ul>");
        for (Map.Entry<String, String> c : controllers.entrySet()) {
            sb.append("<li><b>").append(esc(c.getValue())).append("</b> : ").append(esc(c.getKey())).append("<ul>");
            boolean any = false;
            for (Mapping r : routes.values()) {
                if (!r.getControllerClass().getName().equals(c.getKey())) {
                    continue;
                }
                any = true;
                String href = esc(contextPath) + "/" + esc(r.getUrl());
                if (r.getHttpMethod() == HttpMethod.GET) {
                    sb.append("<li>GET <a href=\"").append(href).append("\">/").append(esc(r.getUrl()))
                      .append("</a> &rarr; ").append(esc(r.label())).append("</li>");
                } else {
                    sb.append("<li><form method=\"post\" action=\"").append(href)
                      .append("\" style=\"display:inline\"><button type=\"submit\">POST /")
                      .append(esc(r.getUrl())).append("</button></form> &rarr; ")
                      .append(esc(r.label())).append("</li>");
                }
            }
            if (!any) {
                sb.append("<li>Aucune methode @UrlMapping</li>");
            }
            sb.append("</ul></li>");
        }
        return sb.append("</ul>").toString();
    }

    /** Texte : GET /emp/list (emp->list), POST /list (test->listPost), ... */
    private String describeRoutes() {
        if (routes.isEmpty()) {
            return "aucune";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Mapping r : routes.values()) {
            joiner.add(r.getUrlMethod() + " (" + r.label() + ")");
        }
        return joiner.toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String jsonEsc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
