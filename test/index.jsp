<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Framework - Page JSP</title>
    <style>
        :root {
            --bg: #0f172a; /* slate-900 */
            --bg-gradient: radial-gradient(1200px 600px at 10% 10%, #1e293b 0%, rgba(15,23,42,0) 60%),
                              radial-gradient(1000px 500px at 90% 20%, #0ea5e9 0%, rgba(14,165,233,0) 40%),
                              linear-gradient(180deg, #0f172a, #0b1220);
            --card: #0b1220;
            --muted: #94a3b8;
            --text: #e2e8f0;
            --brand: #38bdf8; /* sky-400 */
            --brand-strong: #0ea5e9; /* sky-500 */
            --accent: rgba(56,189,248,0.15);
            --ok: #22c55e;
            --warn: #fbbf24;
        }
        * { box-sizing: border-box; }
        html, body { height: 100%; }
        body {
            margin: 0;
            font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, "Helvetica Neue", Arial, "Apple Color Emoji", "Segoe UI Emoji";
            color: var(--text);
            background: var(--bg-gradient), var(--bg);
            display: grid;
            place-items: center;
            padding: 32px 16px;
        }
        .container {
            width: 100%;
            max-width: 1000px;
            background: linear-gradient(180deg, rgba(255,255,255,0.04), rgba(255,255,255,0.02));
            border: 1px solid rgba(148,163,184,0.15);
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(2,6,23,0.45), inset 0 1px 0 rgba(255,255,255,0.05);
            backdrop-filter: blur(6px);
            padding: 28px;
        }
        .header {
            display: flex;
            align-items: center;
            gap: 12px;
            justify-content: center;
            padding-bottom: 16px;
            margin-bottom: 20px;
            border-bottom: 1px dashed rgba(148,163,184,0.2);
        }
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: var(--brand);
            background: var(--accent);
            border: 1px solid rgba(56,189,248,0.35);
            padding: 6px 10px;
            border-radius: 999px;
            font-weight: 600;
            font-size: 14px;
        }
        h1 {
            margin: 0;
            font-size: 28px;
            letter-spacing: 0.3px;
            background: linear-gradient(90deg, #e2e8f0, #38bdf8);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
        }
        .grid {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 16px;
        }
        .panel {
            grid-column: span 12;
            background: rgba(2,6,23,0.35);
            border: 1px solid rgba(148,163,184,0.2);
            border-radius: 12px;
            padding: 16px;
        }
        .panel h3 { margin: 0 0 10px; color: #cbd5e1; font-size: 16px; }
        .kv { display: grid; gap: 6px; }
        .kv p { margin: 0; color: var(--muted); }
        .kv strong { color: #e2e8f0; font-weight: 600; }
        code, kbd {
            font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
            background: rgba(15,23,42,0.8);
            border: 1px solid rgba(148,163,184,0.25);
            padding: 2px 6px;
            border-radius: 6px;
            color: #e2e8f0;
        }
        .servlet-note {
            background: linear-gradient(180deg, rgba(251,191,36,0.18), rgba(251,191,36,0.08));
            border: 1px solid rgba(251,191,36,0.35);
            color: #fde68a;
            padding: 14px 16px;
            border-radius: 10px;
        }
        .test-links {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 8px;
        }
        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 14px;
            border-radius: 10px;
            color: white;
            text-decoration: none;
            background: linear-gradient(180deg, var(--brand), var(--brand-strong));
            border: 1px solid rgba(56,189,248,0.35);
            box-shadow: 0 6px 18px rgba(14,165,233,0.25);
            transition: transform .12s ease, box-shadow .2s ease, filter .2s ease;
            will-change: transform, filter;
        }
        .btn:hover { transform: translateY(-1px); filter: brightness(1.08); box-shadow: 0 10px 22px rgba(14,165,233,0.35); }
        .btn:active { transform: translateY(0); filter: brightness(0.98); }
        .ok-list li::marker { color: var(--ok); }
        .footer { margin-top: 18px; text-align: center; color: var(--muted); font-size: 12px; }
        @media (min-width: 768px) {
            .panel--half { grid-column: span 6; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <span class="badge">🚀 Test Framework</span>
            <h1>Page JSP</h1>
        </div>
        
        <div class="grid">
            <div class="panel panel--half">
                <h3>📋 Informations de la requête</h3>
                <div class="kv">
                    <p><strong>URL :</strong> <code><%= request.getRequestURL() %></code></p>
                    <p><strong>Méthode :</strong> <code><%= request.getMethod() %></code></p>
                    <p><strong>Context Path :</strong> <code><%= request.getContextPath() %></code></p>
                    <p><strong>Servlet Path :</strong> <code><%= request.getServletPath() %></code></p>
                    <p><strong>Path Info :</strong> <code><%= request.getPathInfo() %></code></p>
                    <p><strong>Query :</strong> <code><%= request.getQueryString() != null ? request.getQueryString() : "Aucune" %></code></p>
                </div>
            </div>
            
            <div class="panel panel--half">
                <h3>⏰ Informations temporelles</h3>
                <%
                    Date now = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                %>
                <p><strong>Date/Heure actuelle :</strong> <code><%= formatter.format(now) %></code></p>
                <p><strong>Session ID :</strong> <code><%= session.getId() %></code></p>
            </div>
        </div>

        <div class="servlet-note">
            <h3>⚠️ Note importante</h3>
            <p>Cette page JSP fonctionne car elle est directement accessible. Cependant, toutes les autres URLs sont interceptées par le <strong>FrontServlet</strong> via le mapping <code>/*</code> dans <code>web.xml</code>.</p>
        </div>

        <div class="panel" style="margin-top: 14px;">
            <h3>🔗 Liens de test pour le FrontServlet</h3>
            <p class="kv">Cliquez sur ces liens pour voir le FrontServlet en action :</p>
            <div class="test-links">
                <a class="btn" href="<%= request.getContextPath() %>/frontServlet/GrosseTete">Test GrosseTete</a>
                <a class="btn" href="<%= request.getContextPath() %>/utilisateur/profil">Profil Utilisateur</a>
                <a class="btn" href="<%= request.getContextPath() %>/produit/123">Produit 123</a>
                <a class="btn" href="<%= request.getContextPath() %>/admin/dashboard?user=admin">Dashboard Admin</a>
                <a class="btn" href="<%= request.getContextPath() %>/api/data.json">API JSON</a>
                <a class="btn" href="<%= request.getContextPath() %>/inexistant/chemin/bidon">Chemin inexistant</a>
            </div>
        </div>

        <div class="panel" style="margin-top: 14px;">
            <h3>✅ Test réussi !</h3>
            <p>Si vous voyez cette page, cela signifie que :</p>
            <ul class="ok-list">
                <li>Le framework a été compilé avec succès</li>
                <li>L'application a été déployée dans Tomcat</li>
                <li>Les JSP fonctionnent correctement</li>
                <li>Le serveur est opérationnel</li>
            </ul>
        </div>
        <div class="footer">Contexte: <code><%= request.getContextPath() %></code></div>
    </div>
</body>
</html>
