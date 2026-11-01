package com.app.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.UrlMapping;
import com.framework.model.ModelAndView;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sprint 11 : une seule instance de ce controller pour toute l'application.
 * Le compteur est un attribut de l'instance : avec une instance unique il augmente a chaque appel (tous
 * navigateurs confondus), l'identifiant de l'instance ne change pas. Avant le sprint 11, une nouvelle instance
 * etait creee a chaque requete : le compteur restait a 1.
 */
@Controller("instance")
public class InstanceController {

    private final AtomicInteger appels = new AtomicInteger(); // AtomicInteger : l'instance est partagee entre les threads
    private final String creeLe = LocalTime.now().withNano(0).toString();

    @UrlMapping("instance/compteur")
    public ModelAndView compteur() {
        return page(appels.incrementAndGet());
    }

    /** Autre methode du meme controller : meme instance, donc meme compteur (sans l'incrementer). */
    @UrlMapping("instance/etat")
    public ModelAndView etat() {
        return page(appels.get());
    }

    private ModelAndView page(int nombre) {
        return new ModelAndView("instance/result")
                .addAttribut("appels", nombre)
                .addAttribut("instance", Integer.toHexString(System.identityHashCode(this)))
                .addAttribut("creeLe", creeLe);
    }
}
