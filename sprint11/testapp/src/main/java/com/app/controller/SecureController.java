package com.app.controller;

import com.framework.annotation.APIREST;
import com.framework.annotation.Auth;
import com.framework.annotation.Controller;
import com.framework.annotation.UrlMapping;
import com.framework.model.MaSession;
import com.framework.model.ModelAndView;

/** Sprint 10 : @Auth sur la classe -> toutes les routes de ce controller exigent un utilisateur connecte. */
@Controller("secure")
@Auth
public class SecureController {

    @UrlMapping("secure/dashboard")
    public ModelAndView dashboard(MaSession session) {
        return new ModelAndView("auth/result")
                .addAttribut("titre", "Tableau de bord")
                .addAttribut("message", "@Auth sur la classe SecureController : acces reserve aux utilisateurs connectes.")
                .addAttribut("user", session.getAttribute(AuthController.SESSION_USER))
                .addAttribut("roles", session.getAttribute(AuthController.SESSION_ROLES));
    }

    /** Route JSON protegee : sans connexion, l'erreur 401 est aussi renvoyee en JSON. */
    @UrlMapping("secure/info")
    @APIREST(alreadyJson = true)
    public String info(MaSession session) {
        String user = String.valueOf(session.getAttribute(AuthController.SESSION_USER));
        return "{\"utilisateur\":\"" + user.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"acces\":\"autorise\"}";
    }
}
