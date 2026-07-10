#!/bin/bash

# Arrêter le script à la première erreur
set -e

FRAMEWORK_NAME="sprint-mvc-framework"
VERSION="sprint03"

SRC_DIR="src/main/java"
BUILD_DIR="build/classes"
BUILD_JAR="build/${FRAMEWORK_NAME}.jar"
DIST_DIR="dist"
LIB_DIR="lib"
SERVLET_API_JAR="${LIB_DIR}/jakarta.servlet-api-6.0.0.jar"

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}🔨 Construction du Framework MVC${NC}"
echo -e "${BLUE}Version : ${VERSION}${NC}"
echo -e "${BLUE}=========================================${NC}"

# Vérification du JAR Servlet
if [ ! -f "$SERVLET_API_JAR" ]; then
    echo -e "${RED}❌ Erreur : $SERVLET_API_JAR introuvable.${NC}"
    echo "Copiez jakarta.servlet-api-6.0.0.jar dans le dossier lib du framework."
    exit 1
fi

# Nettoyage
echo -e "${YELLOW}🧹 Nettoyage des anciens builds...${NC}"
rm -rf build dist
mkdir -p "$BUILD_DIR"
mkdir -p "$DIST_DIR"

# Génération de la liste des sources Java
echo -e "${YELLOW}📂 Recherche des sources Java...${NC}"
find "$SRC_DIR" -name "*.java" > sources.txt

if [ ! -s sources.txt ]; then
    echo -e "${RED}❌ Aucun fichier source trouvé dans $SRC_DIR${NC}"
    rm -f sources.txt
    exit 1
fi

echo -e "${YELLOW}🔧 Compilation du framework...${NC}"

if ! javac -cp "$SERVLET_API_JAR" \
    -d "$BUILD_DIR" \
    @sources.txt \
    2> compile_errors.txt
then
    echo -e "${RED}❌ Erreur de compilation du framework !${NC}"
    echo "Détails des erreurs :"
    cat compile_errors.txt
    rm -f sources.txt compile_errors.txt
    exit 1
fi

rm -f sources.txt compile_errors.txt
echo -e "${GREEN}✅ Compilation réussie !${NC}"

# Création du JAR (sans option -m pour éviter l'erreur)
echo -e "${YELLOW}📦 Création du JAR...${NC}"

# Créer un fichier MANIFEST.MF (optionnel, pour la version)
echo "Manifest-Version: 1.0" > MANIFEST.MF
echo "Implementation-Version: ${VERSION}" >> MANIFEST.MF
echo "Created-By: Framework Build Script" >> MANIFEST.MF

# Utiliser l'option -m avec le fichier MANIFEST.MF
jar -cvfm "$BUILD_JAR" MANIFEST.MF -C "$BUILD_DIR" .

# Supprimer le fichier MANIFEST temporaire
rm -f MANIFEST.MF

# Copier le JAR dans les différents emplacements
cp "$BUILD_JAR" "$DIST_DIR/${FRAMEWORK_NAME}-${VERSION}.jar"
cp "$DIST_DIR/${FRAMEWORK_NAME}-${VERSION}.jar" "$FRAMEWORK_NAME.jar"

echo -e "${GREEN}✅ JAR créé avec succès !${NC}"

# Afficher les informations
echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}✅ Construction terminée avec succès !${NC}"
echo -e "${BLUE}📦 JAR généré :${NC}"
echo "   - $DIST_DIR/${FRAMEWORK_NAME}-${VERSION}.jar"
echo "   - $FRAMEWORK_NAME.jar"
echo -e "${BLUE}📋 Version : ${VERSION}${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo -e "${YELLOW}💡 N'oubliez pas de copier le JAR vers l'application test :${NC}"
echo "   cp $FRAMEWORK_NAME.jar ../applicationTest/lib/"