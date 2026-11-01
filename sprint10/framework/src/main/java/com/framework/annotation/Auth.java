package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 : la route n'est accessible qu'a un utilisateur authentifie.
 * Sur une classe : toutes les routes du controller sont protegees.
 * Sur une methode : seule cette route est protegee, et elle est prioritaire sur l'annotation de la classe.
 * Utilisateur non authentifie : reponse HTTP 401.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
}
