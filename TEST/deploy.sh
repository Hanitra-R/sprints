#!/bin/bash

# Arrêter le script en cas d'erreur
set -e

APP_NAME="sprint03"  # ← Changé de sprint02 à sprint03

SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
TEMP_DIR="TEMP"
LIB_DIR="lib"
APP_DIR="app"

# Adapter ce chemin selon ton installation Tomcat
TOMCAT_WEBAPPS="/home/rindra/Documents/tomcat/apache-tomcat-10.0.16/webapps/"

SERVLET_API_JAR="$LIB_DIR/jakarta.servlet-api-6.0.0.jar"
FRAMEWORK_JAR="$LIB_DIR/sprint-mvc-framework.jar"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}🚀 Déploiement de l'application test${NC}"
echo -e "${BLUE}Application : ${APP_NAME}${NC}"
echo -e "${BLUE}=========================================${NC}"

# Vérification du framework
if [ ! -f "$FRAMEWORK_JAR" ]; then
    echo -e "${RED}❌ Erreur : $FRAMEWORK_JAR introuvable.${NC}"
    echo "Construisez d'abord le framework puis copiez son JAR dans $LIB_DIR."
    echo ""
    echo "Pour construire le framework :"
    echo "  cd ../framework"
    echo "  ./build.sh"
    echo "  cp sprint-mvc-framework.jar ../applicationTest/lib/"
    exit 1
fi

# Vérification de la Servlet API
if [ ! -f "$SERVLET_API_JAR" ]; then
    echo -e "${RED}❌ Erreur : $SERVLET_API_JAR introuvable.${NC}"
    echo "Copiez jakarta.servlet-api-6.0.0.jar dans le dossier lib."
    exit 1
fi

# Nettoyage
echo -e "${YELLOW}🧹 Nettoyage des fichiers temporaires...${NC}"
rm -rf "$TEMP_DIR"

mkdir -p "$TEMP_DIR/WEB-INF/classes"
mkdir -p "$TEMP_DIR/WEB-INF/lib"

# Compilation
echo -e "${YELLOW}📂 Recherche des sources Java...${NC}"
if find "$SRC_DIR" -name "*.java" | grep -q .; then
    find "$SRC_DIR" -name "*.java" > sources.txt

    echo -e "${YELLOW}🔧 Compilation de l'application...${NC}"

    if ! javac \
        -cp "$SERVLET_API_JAR:$FRAMEWORK_JAR" \
        -d "$TEMP_DIR/WEB-INF/classes" \
        @sources.txt \
        2> compile_errors.txt
    then
        echo -e "${RED}❌ Erreur de compilation !${NC}"
        echo "Détails des erreurs :"
        cat compile_errors.txt
        rm -f sources.txt compile_errors.txt
        exit 1
    fi

    rm -f sources.txt compile_errors.txt
    echo -e "${GREEN}✅ Compilation réussie !${NC}"
else
    echo -e "${YELLOW}⚠️  Aucun fichier Java trouvé dans $SRC_DIR.${NC}"
fi

# Copier les fichiers web
echo -e "${YELLOW}📂 Copie des fichiers web...${NC}"
cp -r "$WEB_DIR/"* "$TEMP_DIR/" 2>/dev/null || true

# S'assurer que WEB-INF existe
mkdir -p "$TEMP_DIR/WEB-INF"

# Copier web.xml
if [ -f "$WEB_DIR/web.xml" ]; then
    cp "$WEB_DIR/web.xml" "$TEMP_DIR/WEB-INF/web.xml"
else
    echo -e "${YELLOW}⚠️  web.xml non trouvé, création d'un fichier par défaut${NC}"
    cat > "$TEMP_DIR/WEB-INF/web.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    
    <display-name>Sprint MVC Framework</display-name>
    
    <servlet>
        <servlet-name>FrontControllerServlet</servlet-name>
        <servlet-class>mg.hasner.framework.FrontControllerServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>FrontControllerServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
EOF
fi

# Copier les bibliothèques (sauf Servlet API)
echo -e "${YELLOW}📦 Copie des bibliothèques...${NC}"
for jar in "$LIB_DIR"/*.jar
do
    if [ -f "$jar" ] && [ "$(basename "$jar")" != "jakarta.servlet-api-6.0.0.jar" ]; then
        cp "$jar" "$TEMP_DIR/WEB-INF/lib/"
        echo "   - $(basename "$jar")"
    fi
done

# Création du dossier app
mkdir -p "$APP_DIR"

# Création du WAR
echo -e "${YELLOW}📦 Création du fichier WAR...${NC}"
cd "$TEMP_DIR"
jar -cvf "../$APP_DIR/$APP_NAME.war" . > /dev/null
cd ..

# Déploiement vers Tomcat
echo -e "${YELLOW}🚀 Déploiement vers Tomcat...${NC}"

# Suppression de l'ancien déploiement
if [ -f "$TOMCAT_WEBAPPS/$APP_NAME.war" ]; then
    echo "   Suppression de l'ancien WAR..."
    rm -f "$TOMCAT_WEBAPPS/$APP_NAME.war"
fi

if [ -d "$TOMCAT_WEBAPPS/$APP_NAME" ]; then
    echo "   Suppression de l'ancien répertoire..."
    rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"
fi

# Copie du nouveau WAR
cp "$APP_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS"

echo -e "${GREEN}✅ Déploiement terminé avec succès !${NC}"
echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}✅ Application déployée !${NC}"
echo -e "${BLUE}📦 WAR :${NC} $APP_DIR/$APP_NAME.war"
echo -e "${BLUE}🌐 Tomcat :${NC} $TOMCAT_WEBAPPS"
echo -e "${BLUE}🌍 URL :${NC} http://localhost:8080/$APP_NAME/"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "${YELLOW}💡 Pour tester avec curl :${NC}"
echo "   curl -X GET  http://localhost:8080/$APP_NAME/"
echo "   curl -X GET  http://localhost:8080/$APP_NAME/users"
echo "   curl -X POST http://localhost:8080/$APP_NAME/users"