package com.app.controller;

import com.framework.annotation.Auth;
import com.framework.annotation.Controller;
import com.framework.annotation.HttpMethod;
import com.framework.annotation.UrlMapping;
import com.framework.model.MaSession;
import com.framework.model.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/** Sprint 10 : connexion et deconnexion (dans la session HTTP), page publique et page protegee par @Auth.
 *  Sprint 10 bis : l'utilisateur connecte a aussi un role (ou plusieurs) dans la session. */
@Controller("auth")
public class AuthController {

    /** Noms des attributs de session : les memes que les context-param session-user-name et session-role-name de web.xml. */
    public static final String SESSION_USER = "user";
    public static final String SESSION_ROLES = "roles";

    @UrlMapping("auth/login")
    public ModelAndView loginForm() {
        return new ModelAndView("auth/login");
    }

    /**
     * Connexion de demonstration : n'importe quel nom non vide est accepte (pas de mot de passe).
     * role : vide (aucun role), un role ("ADMIN"), ou plusieurs separes par une virgule ("RH,ADMIN") qui sont
     * alors gardes dans la session sous forme de liste.
     */
    @UrlMapping(value = "auth/login", method = HttpMethod.POST)
    public ModelAndView login(String nom, String role, MaSession session) {
        if (nom == null || nom.isBlank()) {
            return new ModelAndView("auth/login").addAttribut("erreur", "Le nom est obligatoire.");
        }
        session.setAttribute(SESSION_USER, nom.trim());
        session.setAttribute(SESSION_ROLES, parseRoles(role)); // null : l'attribut est supprime
        return page("Connecte", "L'utilisateur et ses roles sont maintenant dans la session.", session);
    }

    @UrlMapping(value = "auth/logout", method = HttpMethod.POST)
    public ModelAndView logout(MaSession session) {
        session.removeAttribute(SESSION_USER);
        session.removeAttribute(SESSION_ROLES);
        return page("Deconnecte", "L'utilisateur et ses roles ont ete retires de la session.", session);
    }

    /** Route sans @Auth : accessible a tout le monde. */
    @UrlMapping("auth/public")
    public ModelAndView publique(MaSession session) {
        return page("Page publique", "Pas de @Auth : accessible sans etre connecte.", session);
    }

    /** @Auth sur la methode : reponse 401 si personne n'est connecte. */
    @Auth
    @UrlMapping("auth/profil")
    public ModelAndView profil(MaSession session) {
        return page("Profil", "@Auth sur la methode : seuls les utilisateurs connectes passent.", session);
    }

    /** "" -> null ; "ADMIN" -> "ADMIN" (String) ; "RH,ADMIN" -> liste [RH, ADMIN]. */
    private Object parseRoles(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        if (!role.contains(",")) {
            return role.trim();
        }
        List<String> roles = new ArrayList<>();
        for (String r : role.split(",")) {
            if (!r.isBlank()) {
                roles.add(r.trim());
            }
        }
        return roles;
    }

    private ModelAndView page(String titre, String message, MaSession session) {
        return new ModelAndView("auth/result")
                .addAttribut("titre", titre)
                .addAttribut("message", message)
                .addAttribut("user", session.getAttribute(SESSION_USER))
                .addAttribut("roles", session.getAttribute(SESSION_ROLES));
    }
}
