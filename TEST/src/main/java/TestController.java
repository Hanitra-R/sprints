package mg.hasner.test;

import mg.hasner.framework.Controller;
import mg.hasner.framework.UrlMapping;
import mg.hasner.framework.HttpMethod;

@Controller
public class TestController {
    
    @UrlMapping(value = "/users", method = HttpMethod.GET)
    public void getUsers() {
        System.out.println("📋 GET /users - Liste des utilisateurs");
    }
    
    @UrlMapping(value = "/users", method = HttpMethod.POST)
    public void createUser() {
        System.out.println("➕ POST /users - Création d'un utilisateur");
    }
    
    @UrlMapping(value = "/users/{id}", method = HttpMethod.GET)
    public void getUserById() {
        System.out.println("🔍 GET /users/{id} - Détail d'un utilisateur");
    }
    
    @UrlMapping(value = "/users/{id}", method = HttpMethod.DELETE)
    public void deleteUser() {
        System.out.println("🗑️ DELETE /users/{id} - Suppression d'un utilisateur");
    }
    
    @UrlMapping(value = "/hello", method = HttpMethod.GET)
    public void sayHello() {
        System.out.println("👋 GET /hello - Bonjour !");
    }
}