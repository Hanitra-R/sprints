package com.app.controller;

import com.framework.annotation.Auth;
import com.framework.annotation.Controller;
import com.framework.annotation.HttpMethod;
import com.framework.annotation.UrlMapping;
import com.framework.model.MaSession;
import com.framework.model.ModelAndView;

/** Sprint 10 : connexion et deconnexion (dans la session HTTP), page publique et page protegee par @Auth. */
@Controller("auth")
public class AuthController {

    /** Nom de l'attribut de session : le meme que le context-param session-user-name de web.xml. */
    public static final String SESSION_USER = "user";

    @UrlMapping("auth/login")
    public ModelAndView loginForm() {
        return new ModelAndView("auth/login");
    }

    /** Connexion de demonstration : n'importe quel nom non vide est accepte (pas de mot de passe). */
    @UrlMapping(value = "auth/login", method = HttpMethod.POST)
    public ModelAndView login(String nom, MaSession session) {
        if (nom == null || nom.isBlank()) {
            return new ModelAndView("auth/login").addAttribut("erreur", "Le nom est obligatoire.");
        }
        session.setAttribute(SESSION_USER, nom.trim());
        return page("Connecte", "L'utilisateur est maintenant dans la session.", session);
    }

    @UrlMapping(value = "auth/logout", method = HttpMethod.POST)
    public ModelAndView logout(MaSession session) {
        session.removeAttribute(SESSION_USER);
        return page("Deconnecte", "L'utilisateur a ete retire de la session.", session);
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

    private ModelAndView page(String titre, String message, MaSession session) {
        return new ModelAndView("auth/result")
                .addAttribut("titre", titre)
                .addAttribut("message", message)
                .addAttribut("user", session.getAttribute(SESSION_USER));
    }
}
