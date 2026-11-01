# Framework MVC — Sprint 10

## Authentification : annotation `@Auth`

Le sprint 10 reprend le framework et l'application de démonstration du sprint 9, y compris la session `MaSession`. Il ajoute l'annotation `@Auth` : une route marquée `@Auth` n'est exécutée que si un utilisateur est connecté, c'est-à-dire si la session HTTP contient l'attribut qui désigne l'utilisateur. Sinon le framework répond **401** et n'appelle pas la méthode du contrôleur.

`@Auth` s'applique au niveau de la classe ou de la méthode :

```java
@Controller("secure")
@Auth                                   // toutes les routes de ce contrôleur sont protégées
public class SecureController { ... }

@Auth                                   // seule cette route est protégée
@UrlMapping("auth/profil")
public ModelAndView profil(MaSession session) { ... }
```

- Sans `@Auth`, la route reste publique.
- Si la méthode et la classe portent `@Auth`, l'annotation de la méthode est prioritaire.

### Nom de l'attribut de session

Le développeur déclare dans `web.xml` le nom de l'attribut de session qui contient l'utilisateur connecté :

```xml
<context-param>
    <param-name>session-user-name</param-name>   <!-- session-log-name est accepté aussi -->
    <param-value>user</param-value>
</context-param>
```

Le `FrontControllerListener` lit ce paramètre au démarrage et le range dans le `ServletContext` (`framework.sessionUserName`), où le `FrontControllerServlet` le retrouve. Si une route est annotée `@Auth` alors que ce paramètre manque, l'application refuse de démarrer.

Le contrôleur de connexion doit ranger l'utilisateur dans la session sous ce même nom :

```java
session.setAttribute("user", nom);      // "user" = valeur de session-user-name
```

### Vérification dans le FrontController

`checkAuthorization` est appelée avant l'invocation de la méthode du contrôleur :

1. cherche `@Auth` sur la méthode, puis sur la classe si la méthode n'en a pas ; sans `@Auth`, la requête continue ;
2. lit la session avec `getSession(false)` (une requête anonyme ne crée donc pas de session) ;
3. si la session n'existe pas ou ne contient pas l'attribut de l'utilisateur, appelle `handleUnauthorized` et retourne `false` : le traitement de la requête s'arrête ;
4. sinon retourne `true` et la méthode du contrôleur est exécutée.

### Erreur 401 : `handleUnauthorized`

La réponse a le statut HTTP **401** (`HttpServletResponse.SC_UNAUTHORIZED`). Son format dépend de la requête :

- **JSON** si la route est `@APIREST`, ou si le client demande `application/json` sans demander `text/html` :
  `{"status":401,"error":"Non authentifie","message":"Authentification requise pour acceder a cette ressource."}`
- **HTML** sinon (navigateur) : une page « 401 - Non authentifie ».

### Application de démonstration

- `GET /auth/login` : formulaire de connexion ;
- `POST /auth/login` : range le nom saisi dans la session (`user`) ;
- `POST /auth/logout` : retire l'utilisateur de la session ;
- `GET /auth/public` : route sans `@Auth` ;
- `GET /auth/profil` : `@Auth` sur la méthode ;
- `GET /secure/dashboard` : `@Auth` sur la classe `SecureController` ;
- `GET /secure/info` : route `@APIREST` protégée, qui répond en JSON.

Pour tester : ouvre `/testapp/auth/profil` sans te connecter (401), connecte-toi sur `/testapp/auth/login`, puis ouvre `auth/profil`, `secure/dashboard` et `secure/info` (accès accordé). Clique sur « Se déconnecter » : `auth/profil` répond de nouveau 401.

## Build et déploiement

Depuis ce dossier :

```bat
.\tomcat-start.bat
.\deploy.bat
```

L'application s'ouvre sur `http://localhost:8080/testapp/`.