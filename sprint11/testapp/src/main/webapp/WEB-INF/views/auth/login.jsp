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
<head><meta charset="UTF-8"><title>Connexion</title>
<style>body{font-family:Arial,sans-serif;max-width:560px;margin:2rem auto;padding:0 1rem}input,button{margin:.3rem 0;padding:.4rem}.err{color:#c00}</style>
</head>
<body>
<h1>Connexion</h1>
<% if (request.getAttribute("erreur") != null) { %>
<p class="err"><%= e(request.getAttribute("erreur")) %></p>
<% } %>
<form method="post" action="<%= contextPath %>/auth/login">
    <input type="text" name="nom" placeholder="Nom d'utilisateur" required><br>
    <select name="role">
        <option value="">(aucun role)</option>
        <option value="USER">USER</option>
        <option value="RH">RH</option>
        <option value="ADMIN">ADMIN</option>
        <option value="RH,ADMIN">RH + ADMIN (plusieurs roles : liste)</option>
    </select><br>
    <button type="submit">Se connecter</button>
</form>
<p><a href="<%= contextPath %>/auth/public">Page publique</a></p>
</body>
</html>
