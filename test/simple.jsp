<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Page JSP Simple</title>
    <style>
        body { font-family: Arial; margin: 40px; }
        .box { background: #f0f0f0; padding: 20px; border-radius: 5px; }
    </style>
</head>
<body>
    <h2>📄 Page JSP Simple</h2>
    
    <div class="box">
        <h3>Détails de la requête :</h3>
        <p><b>URL :</b> <%= request.getRequestURL() %></p>
        <p><b>Paramètres :</b> <%= request.getQueryString() %></p>
        <p><b>User-Agent :</b> <%= request.getHeader("User-Agent") %></p>
    </div>
    
    <p><a href="index.jsp">← Retour à l'index</a></p>
    
    <h3>Test du FrontServlet :</h3>
    <ul>
        <li><a href="test/servlet">Lien vers servlet</a></li>
        <li><a href="autre/page">Autre page</a></li>
    </ul>
</body>
</html>
