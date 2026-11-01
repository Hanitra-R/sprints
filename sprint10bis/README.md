# Framework MVC — Sprint 10 bis

## Rôles : `@Auth(roles = …)` et erreur 403

Le sprint 10 bis reprend le framework et l'application de démonstration du sprint 10. Il ajoute les rôles : `@Auth` accepte un attribut `roles`, et la route n'est exécutée que si l'utilisateur connecté possède au moins un des rôles demandés. Sinon le framework répond **403** et n'appelle pas la méthode du contrôleur.

```java
@Controller("admin")
@Auth(roles = "ADMIN")                          // toutes les routes : rôle ADMIN
public class AdminController {

    @Auth(roles = {"RH", "ADMIN"})              // cette route : RH ou ADMIN
    @UrlMapping("admin/rh")
    public ModelAndView rh(MaSession session) { ... }

    @Auth                                       // cette route : il suffit d'être connecté
    @UrlMapping("admin/connecte")
    public ModelAndView connecte(MaSession session) { ... }
}
```

- `roles` est un tableau de `String`, vide par défaut : `@Auth` seul accepte tout utilisateur connecté (comportement du sprint 10).
- Quand plusieurs rôles sont listés, un seul suffit.
- La comparaison est exacte : les majuscules et minuscules comptent.
- Si la méthode et la classe portent `@Auth`, celui de la méthode remplace entièrement celui de la classe, rôles compris.

### Rôles dans la session

Le développeur déclare dans `web.xml` le nom de l'attribut de session qui contient les rôles :

```xml
<context-param>
    <param-name>session-role-name</param-name>
    <param-value>roles</param-value>
</context-param>
```

Le `FrontControllerListener` lit ce paramètre au démarrage et le range dans le `ServletContext` (`framework.sessionRoleName`), à côté du nom de l'utilisateur (`session-user-name`). Si une route utilise `roles` alors que ce paramètre manque, l'application refuse de démarrer.

Le contrôleur de connexion range les rôles dans la session sous ce nom. La valeur peut être :

- une `String` : `"ADMIN"` ;
- une collection : `List.of("RH", "ADMIN")` ;
- un tableau : `new String[]{"RH", "ADMIN"}`.

### Vérification dans le FrontController

`checkAuthorization` s'exécute avant la méthode du contrôleur :

1. cherche `@Auth` sur la méthode, puis sur la classe ; sans `@Auth`, la requête continue ;
2. si l'utilisateur n'est pas connecté, répond **401** : un anonyme n'obtient jamais de 403 ;
3. si `roles` est vide, la requête continue ;
4. lit l'attribut des rôles dans la session et vérifie qu'au moins un rôle demandé s'y trouve ;
5. sinon appelle `handleForbidden` et retourne `false` : la méthode du contrôleur n'est pas appelée.

### Erreur 403 : `handleForbidden`

La réponse a le statut HTTP **403** (`HttpServletResponse.SC_FORBIDDEN`). Le format suit la même règle que le 401 :

- **JSON** si la route est `@APIREST`, ou si le client demande `application/json` sans demander `text/html` ;
- **HTML** sinon : une page « 403 - Acces refuse ».

Le message envoyé au client ne révèle pas les rôles exigés. Ils sont écrits dans le journal du serveur (Tomcat).

### Application de démonstration

- `GET /auth/login` : formulaire avec le nom et le rôle (aucun, `USER`, `RH`, `ADMIN`, ou « RH,ADMIN » qui range une liste) ;
- `POST /auth/login` : range le nom et les rôles dans la session ;
- `POST /auth/logout` : retire le nom et les rôles de la session ;
- `GET /admin/dashboard` : `@Auth(roles = "ADMIN")` sur la classe `AdminController` ;
- `GET /admin/rh` : `@Auth(roles = {"RH", "ADMIN"})` sur la méthode ;
- `GET /admin/connecte` : `@Auth` sans rôle sur la méthode, il suffit d'être connecté ;
- `GET /admin/api` : route `@APIREST` qui répond en JSON, rôle `ADMIN`.

Pour tester : ouvre `/testapp/admin/dashboard` sans te connecter (401). Connecte-toi sur `/testapp/auth/login` avec le rôle `RH` : `admin/rh` répond mais `admin/dashboard` donne 403. Reconnecte-toi avec `ADMIN` ou `RH,ADMIN` : toutes les routes `admin/…` répondent. Après « Se déconnecter », elles redonnent 401.

## Build et déploiement

Depuis ce dossier :

```bat
.\tomcat-start.bat
.\deploy.bat
```

L'application s'ouvre sur `http://localhost:8080/testapp/`.
