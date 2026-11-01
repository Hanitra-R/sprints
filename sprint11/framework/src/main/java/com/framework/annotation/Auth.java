package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 : la route n'est accessible qu'a un utilisateur authentifie.
 * Sur une classe : toutes les routes du controller sont protegees.
 * Sur une methode : seule cette route est protegee, et elle est prioritaire sur l'annotation de la classe
 * (roles compris : @Auth sans roles sur une methode annule les roles exiges par la classe).
 * Utilisateur non authentifie : reponse HTTP 401.
 *
 * Sprint 10 bis : roles = roles autorises. Vide (par defaut) : l'authentification suffit.
 * Sinon l'utilisateur doit avoir au moins un de ces roles, sinon reponse HTTP 403.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

    /** Roles autorises ; si la liste est vide, seule l'authentification est necessaire. */
    String[] roles() default {};
}
