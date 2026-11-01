package com.framework.listener;

import com.framework.annotation.Auth;
import com.framework.util.ClasseUtilitaire;
import com.framework.util.Mapping;
import com.framework.util.UploadUtil;
import com.framework.util.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Map;

/**
 * Scanne les controllers UNE SEULE FOIS au demarrage de l'application
 * et stocke le resultat dans le ServletContext.
 */
public class FrontControllerListener implements ServletContextListener {

    public static final String PARAM_PACKAGE = "controllerPackage";
    public static final String DEFAULT_PACKAGE = "com.app.controller";

    public static final String ATTR_PACKAGE = "framework.controllerPackage";
    public static final String ATTR_CONTROLLERS = "framework.controllers";
    public static final String ATTR_ROUTES = "framework.routes";

    /** Sprint 8 : dossier ou MapFile.save() enregistre les fichiers recus (context-param upload-dir). */
    public static final String PARAM_UPLOAD_DIR = "upload-dir";
    public static final String ATTR_UPLOAD_DIR = "framework.uploadDir";

    /** Sprint 10 : nom de l'attribut de session qui contient l'utilisateur connecte (context-param session-user-name ; session-log-name est accepte aussi). */
    public static final String PARAM_SESSION_USER = "session-user-name";
    public static final String PARAM_SESSION_LOG = "session-log-name";
    public static final String ATTR_SESSION_USER = "framework.sessionUserName";

    /** Sprint 10 bis : nom de l'attribut de session qui contient le ou les roles de l'utilisateur (context-param session-role-name). */
    public static final String PARAM_SESSION_ROLE = "session-role-name";
    public static final String ATTR_SESSION_ROLE = "framework.sessionRoleName";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = context.getInitParameter(PARAM_PACKAGE);
        if (controllerPackage == null || controllerPackage.isBlank()) {
            controllerPackage = DEFAULT_PACKAGE;
        }

        // Une route en double leve une IllegalStateException : l'application ne demarre pas.
        Map<String, String> controllers = ClasseUtilitaire.getControllers(controllerPackage);
        Map<UrlMethod, Mapping> routes = ClasseUtilitaire.getRoutes(controllerPackage);

        context.setAttribute(ATTR_PACKAGE, controllerPackage);
        context.setAttribute(ATTR_CONTROLLERS, controllers);
        context.setAttribute(ATTR_ROUTES, routes);

        // Sprint 8 : dossier d'upload (${variable} acceptee, ex. ${catalina.base}/uploads) ; une variable inconnue empeche le demarrage
        String uploadDir = context.getInitParameter(PARAM_UPLOAD_DIR);
        if (uploadDir != null && !uploadDir.isBlank()) {
            String resolved = UploadUtil.resolveDir(uploadDir);
            context.setAttribute(ATTR_UPLOAD_DIR, resolved);
            context.log("Dossier d'upload : " + resolved);
        } else {
            context.log("Aucun upload-dir dans web.xml : MapFile.save() sera refuse");
        }

        // Sprint 10 : nom de l'attribut de session de l'utilisateur connecte, lu dans web.xml et garde dans le contexte
        String userName = context.getInitParameter(PARAM_SESSION_USER);
        if (userName == null || userName.isBlank()) {
            userName = context.getInitParameter(PARAM_SESSION_LOG);
        }
        if (userName != null && !userName.isBlank()) {
            context.setAttribute(ATTR_SESSION_USER, userName.trim());
            context.log("Attribut de session de l'utilisateur : " + userName.trim());
        } else if (usesAuth(routes)) {
            // une route @Auth sans nom d'attribut ne pourrait jamais etre autorisee : l'application ne demarre pas
            throw new IllegalStateException("Une route est annotee @Auth : declarer le context-param "
                    + PARAM_SESSION_USER + " (ou " + PARAM_SESSION_LOG + ") dans web.xml");
        }

        // Sprint 10 bis : nom de l'attribut de session des roles (un role String, ou une Collection / liste / tableau de roles)
        String roleName = context.getInitParameter(PARAM_SESSION_ROLE);
        if (roleName != null && !roleName.isBlank()) {
            context.setAttribute(ATTR_SESSION_ROLE, roleName.trim());
            context.log("Attribut de session des roles : " + roleName.trim());
        } else if (usesRoles(routes)) {
            throw new IllegalStateException("Une route est annotee @Auth(roles = ...) : declarer le context-param "
                    + PARAM_SESSION_ROLE + " dans web.xml");
        }

        context.log("FrontControllerListener : " + controllers.size() + " controller(s), "
                + routes.size() + " route(s) dans " + controllerPackage);
        for (Mapping route : routes.values()) {
            context.log("Route : " + route.getUrlMethod() + " -> " + route.label());
        }
    }

    /** Sprint 10 : une route au moins est protegee par @Auth (sur la methode ou sur la classe). */
    private static boolean usesAuth(Map<UrlMethod, Mapping> routes) {
        for (Mapping route : routes.values()) {
            if (route.getMethod().isAnnotationPresent(Auth.class)
                    || route.getControllerClass().isAnnotationPresent(Auth.class)) {
                return true;
            }
        }
        return false;
    }

    /** Sprint 10 bis : une route au moins exige des roles (@Auth(roles = ...) sur la methode ou sur la classe). */
    private static boolean usesRoles(Map<UrlMethod, Mapping> routes) {
        for (Mapping route : routes.values()) {
            Auth onMethod = route.getMethod().getAnnotation(Auth.class);
            Auth onClass = route.getControllerClass().getAnnotation(Auth.class);
            if ((onMethod != null && onMethod.roles().length > 0) || (onClass != null && onClass.roles().length > 0)) {
                return true;
            }
        }
        return false;
    }
}
