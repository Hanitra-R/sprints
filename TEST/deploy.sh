#!/bin/bash

<<<<<<< Updated upstream
# Arrêter le script en cas d'erreur
set -e

APP_NAME="sprint02"

=======
# Définition des variables
APP_NAME="Sprint0304"
>>>>>>> Stashed changes
SRC_DIR="src/main/java"
# Dans ton projet les JSP et le web.xml sont dans src/main/webapps
WEB_DIR="src/main/webapps"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/rindra/Documents/tomcat/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR=$(echo lib/*.jar | tr ' ' ':')

# Nettoyage et création du répertoire temporaire
# rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# Compilation des fichiers Java avec le JAR des Servlets
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR/WEB-INF/classes" @sources.txt
rm -f sources.txt

<<<<<<< Updated upstream
# Vérification du framework
if [ ! -f "$FRAMEWORK_JAR" ]; then
    echo "Erreur : $FRAMEWORK_JAR introuvable."
    echo "Construisez d'abord le framework puis copiez son JAR dans $LIB_DIR."
    exit 1
fi

# Nettoyage
rm -rf "$TEMP_DIR"

mkdir -p "$TEMP_DIR/WEB-INF/classes"
mkdir -p "$TEMP_DIR/WEB-INF/lib"

# Compilation
if find "$SRC_DIR" -name "*.java" | grep -q .; then

    find "$SRC_DIR" -name "*.java" > sources.txt

    echo "Compilation en cours..."

    if ! javac \
        -cp "$SERVLET_API_JAR:$FRAMEWORK_JAR" \
        -d "$TEMP_DIR/WEB-INF/classes" \
        @sources.txt \
        2> compile_errors.txt
    then
        echo "Erreur de compilation !"
        cat compile_errors.txt
        cat sources.txt
        exit 1
    fi

    rm -f sources.txt
    rm -f compile_errors.txt

else
    echo "Aucun fichier Java trouvé dans $SRC_DIR."
fi

# Copier tous les fichiers du webapp sauf web.xml
cp -r "$WEB_DIR/"* "$TEMP_DIR/"

# Recréer WEB-INF
mkdir -p "$TEMP_DIR/WEB-INF"

# Copier web.xml
cp "$WEB_DIR/web.xml" "$TEMP_DIR/WEB-INF/web.xml"

# Copier les bibliothèques sauf Servlet API
for jar in "$LIB_DIR"/*.jar
do
    if [ "$(basename "$jar")" != "jakarta.servlet-api-6.0.0.jar" ]; then
        cp "$jar" "$TEMP_DIR/WEB-INF/lib/"
    fi
done

# Création du dossier app
mkdir -p "$APP_DIR"

# Création du WAR
cd "$TEMP_DIR"
jar -cvf "../$APP_DIR/$APP_NAME.war" .
cd ..

# Suppression de l'ancien déploiement
rm -f "$TOMCAT_WEBAPPS/$APP_NAME.war"
rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"

# Déploiement
cp "$APP_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS"

echo
echo "====================================="
echo "Déploiement terminé avec succès !"
echo "WAR : $APP_DIR/$APP_NAME.war"
echo "Tomcat : $TOMCAT_WEBAPPS"
echo "====================================="
=======
# Copier les fichiers web (web.xml, JSP, etc.)
cp -r "$WEB_DIR/"* "$BUILD_DIR/"
# On déplace le web.xml à sa place standard dans WEB-INF
if [ -f "$BUILD_DIR/web.xml" ]; then
  mv -f "$BUILD_DIR/web.xml" "$BUILD_DIR/WEB-INF/web.xml"
fi

# Copier les librairies nécessaires dans WEB-INF/lib (optionnel mais recommandé)
cp -f "$LIB_DIR"/*.jar "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null || true

# Générer le fichier .war dans le dossier build
cd "$BUILD_DIR" || exit 
jar -cvf "$APP_NAME.war" *
cd ..

# Déploiement dans Tomcat
cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo ""
echo "Déploiement terminé. Copie de $APP_NAME.war dans $TOMCAT_WEBAPPS."
echo "Redémarrez Tomcat si nécessaire."
echo ""
>>>>>>> Stashed changes
