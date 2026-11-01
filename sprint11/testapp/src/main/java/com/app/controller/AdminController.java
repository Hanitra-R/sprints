package com.app.controller;

import com.framework.annotation.APIREST;
import com.framework.annotation.Auth;
import com.framework.annotation.Controller;
import com.framework.annotation.UrlMapping;
import com.framework.model.MaSession;
import com.framework.model.ModelAndView;

/**
 * Sprint 10 bis : roles. @Auth(roles = "ADMIN") sur la classe : par defaut il faut le role ADMIN.
 * Une methode avec son propre @Auth remplace entierement celui de la classe.
 * Sans connexion : 401. Connecte mais sans le bon role : 403.
 */
@Controller("admin")
@Auth(roles = "ADMIN")
public class AdminController {

    /** Pas de @Auth sur la methode : celui de la classe s'applique (role ADMIN). */
    @UrlMapping("admin/dashboard")
    public ModelAndView dashboard(MaSession session) {
        return page("Administration", "Role ADMIN exige (@Auth de la classe).", session);
    }

    /** @Auth de la methode prioritaire : RH ou ADMIN. */
    @Auth(roles = {"RH", "ADMIN"})
    @UrlMapping("admin/rh")
    public ModelAndView rh(MaSession session) {
        return page("Espace RH", "Role RH ou ADMIN exige (@Auth de la methode).", session);
    }

    /** @Auth sans roles sur la methode : tout utilisateur connecte passe, meme sans role. */
    @Auth
    @UrlMapping("admin/connecte")
    public ModelAndView connecte(MaSession session) {
        return page("Espace connecte", "@Auth sans roles sur la methode : il suffit d'etre connecte.", session);
    }

    /** Route JSON : sans le role ADMIN, l'erreur 403 est aussi renvoyee en JSON. */
    @UrlMapping("admin/api")
    @APIREST(alreadyJson = true)
    public String api() {
        return "{\"acces\":\"ADMIN\"}";
    }

    private ModelAndView page(String titre, String message, MaSession session) {
        return new ModelAndView("auth/result")
                .addAttribut("titre", titre)
                .addAttribut("message", message)
                .addAttribut("user", session.getAttribute(AuthController.SESSION_USER))
                .addAttribut("roles", session.getAttribute(AuthController.SESSION_ROLES));
    }
}
