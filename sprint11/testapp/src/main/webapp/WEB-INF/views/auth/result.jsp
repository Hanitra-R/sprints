<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    private static String e(Object o) {
        if (o == null) return "";
        return String.valueOf(o).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
%>
<%
    String contextPath = request.getContextPath();
    Object user = request.getAttribute("user");
    Object roles = request.getAttribute("roles");
%>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><title><%= e(request.getAttribute("titre")) %></title>
<style>body{font-family:Arial,sans-serif;max-width:560px;margin:2rem auto;padding:0 1rem}li{margin:.3rem 0}</style>
</head>
<body>
<h1><%= e(request.getAttribute("titre")) %></h1>
<p><%= e(request.getAttribute("message")) %></p>
<p>Utilisateur connecte :
    <% if (user != null) { %><b><%= e(user) %></b><% } else { %><i>personne</i><% } %></p>
<p>Roles dans la session :
    <% if (roles != null) { %><b><%= e(roles) %></b><% } else { %><i>aucun</i><% } %></p>

<h3>Tester les acces</h3>
<ul>
    <li><a href="<%= contextPath %>/auth/public">auth/public</a> : public</li>
    <li><a href="<%= contextPath %>/auth/profil">auth/profil</a> : @Auth sur la methode</li>
    <li><a href="<%= contextPath %>/secure/dashboard">secure/dashboard</a> : @Auth sur la classe</li>
    <li><a href="<%= contextPath %>/secure/info">secure/info</a> : JSON protege</li>
    <li><a href="<%= contextPath %>/admin/dashboard">admin/dashboard</a> : role ADMIN (classe)</li>
    <li><a href="<%= contextPath %>/admin/rh">admin/rh</a> : role RH ou ADMIN (methode)</li>
    <li><a href="<%= contextPath %>/admin/connecte">admin/connecte</a> : @Auth sans roles (methode)</li>
    <li><a href="<%= contextPath %>/admin/api">admin/api</a> : JSON, role ADMIN</li>
</ul>

<p><a href="<%= contextPath %>/auth/login">Se connecter</a></p>
<form method="post" action="<%= contextPath %>/auth/logout">
    <button type="submit">Se deconnecter</button>
</form>
<p><a href="<%= contextPath %>/">Accueil (toutes les routes)</a></p>
</body>
</html>
