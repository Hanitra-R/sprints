# Framework MVC — Sprint 11

## Performance : une seule instance par contrôleur

Le sprint 11 reprend le framework et l'application de démonstration du sprint 10 bis (authentification, rôles, session, upload). Jusqu'ici le `FrontControllerServlet` créait un nouvel objet contrôleur à chaque requête. Il crée maintenant **une seule instance par classe de contrôleur** et la réutilise pour toutes les requêtes suivantes.

### Le `FrontControllerListener`

Au démarrage de l'application, après le scan des routes, le listener crée une `HashMap` vide et la range dans le `ServletContext` :

```java
context.setAttribute("controllersInstance", new HashMap<Class<?>, Object>());
```

La clé de la map est la classe du contrôleur, la valeur est son unique instance.

### Le `FrontControllerServlet`

Au lieu d'appeler `newInstance()` à chaque requête, la méthode `getControllerInstance(Class<?>)` :

1. lit la map `controllersInstance` dans le `ServletContext` ;
2. si la classe du contrôleur s'y trouve déjà, retourne l'instance existante ;
3. sinon crée l'instance avec le constructeur sans argument, la range dans la map, puis la retourne.

- L'instance est créée à la première requête qui vise ce contrôleur, pas au démarrage.
- L'accès à la map est synchronisé : si deux requêtes arrivent en même temps sur un contrôleur qui n'existe pas encore, une seule instance est créée.
- Si la map est absente du contexte, le servlet lève une `ServletException` qui rappelle de déclarer `com.framework.listener.FrontControllerListener` dans `web.xml`.

### Attention : l'instance est partagée

Une même instance sert tous les utilisateurs et toutes les requêtes simultanées. Un contrôleur ne doit donc pas garder dans ses champs des données propres à un utilisateur ou à une requête :

- les données d'un utilisateur vont dans `MaSession` ;
- les données d'une requête vont dans les paramètres de la méthode et dans le `ModelAndView` ;
- un champ partagé volontairement (un compteur, par exemple) doit être sûr en concurrence, comme un `AtomicInteger`.

### Application de démonstration

Le contrôleur `InstanceController` garde dans ses champs un compteur d'appels, son identifiant (`identityHashCode` en hexadécimal) et son heure de création :

- `GET /instance/compteur` : ajoute 1 au compteur et affiche le compteur, l'identifiant de l'instance et son heure de création ;
- `GET /instance/etat` : affiche les mêmes valeurs sans modifier le compteur.

Pour tester : ouvre `/testapp/instance/compteur` et recharge la page. Le compteur augmente à chaque appel, alors que l'identifiant et l'heure de création ne changent pas. Ouvre ensuite la page dans un autre navigateur : le compteur continue et l'identifiant est le même, car l'instance est partagée. Avec une nouvelle instance par requête, le compteur resterait à 1. Si tu redéploies l'application, une nouvelle instance est créée et le compteur repart de 1.

## Build et déploiement

Depuis ce dossier :

```bat
.\tomcat-start.bat
.\deploy.bat
```

L'application s'ouvre sur `http://localhost:8080/testapp/`.
