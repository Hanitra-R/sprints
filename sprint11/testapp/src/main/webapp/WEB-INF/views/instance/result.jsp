<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    private static String e(Object o) {
        if (o == null) return "";
        return String.valueOf(o).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
%>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><title>Instance du controller</title>
<style>body{font-family:Arial,sans-serif;max-width:560px;margin:2rem auto;padding:0 1rem}li{margin:.3rem 0}code{background:#eee;padding:0 .3rem}</style>
</head>
<body>
<h1>Instance unique du controller</h1>
<ul>
    <li>Appels de <code>compteur</code> depuis le demarrage : <b><%= e(request.getAttribute("appels")) %></b></li>
    <li>Identifiant de l'instance : <b><%= e(request.getAttribute("instance")) %></b></li>
    <li>Instance creee a : <b><%= e(request.getAttribute("creeLe")) %></b></li>
</ul>
<p>Avec une seule instance par controller, le nombre d'appels augmente a chaque rechargement de
<code>instance/compteur</code>, et l'identifiant et l'heure de creation ne changent pas
(meme avec un autre navigateur). Une nouvelle instance a chaque requete donnerait toujours 1.</p>
<ul>
    <li><a href="<%= contextPath %>/instance/compteur">instance/compteur</a> : +1 a chaque appel</li>
    <li><a href="<%= contextPath %>/instance/etat">instance/etat</a> : lit le meme compteur sans l'augmenter</li>
</ul>
<p><a href="<%= contextPath %>/">Accueil (toutes les routes)</a></p>
</body>
</html>
