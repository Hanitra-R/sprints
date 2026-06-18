@echo off
setlocal ENABLEDELAYEDEXPANSION

rem === Configuration des chemins (adapte de votre Commande.bat) ===
set PROJECT_DIR=C:\Users\msimi\Desktop\BOSY\s5\naina\SPRINT
set TOMCAT_DIR=C:\Users\msimi\Documents\apache-tomcat-10.1.28
set TOMCAT_WEBAPPS=%TOMCAT_DIR%\webapps
set SERVLET_API_JAR=%PROJECT_DIR%\test\WEB-INF\lib\servlet-api.jar

rem Verification que servlet-api.jar existe
if not exist "%SERVLET_API_JAR%" (
  echo [ERREUR] servlet-api.jar introuvable: %SERVLET_API_JAR%
  echo Verifiez que servlet-api.jar est bien present dans test\WEB-INF\lib\
  pause
  exit /b 1
)

set SRC_DIR=framework\src
set BUILD_DIR=framework\build
set DIST_DIR=framework\dist
set PKG_DIR=com\myframework

mkdir "%BUILD_DIR%" 2>nul
mkdir "%DIST_DIR%" 2>nul
mkdir "test\WEB-INF\lib" 2>nul

echo [1/4] Compilation du framework (toutes les sources)...
rem Lister toutes les sources Java recursivement
del /f /q sources.txt 2>nul
for /r "%SRC_DIR%" %%f in (*.java) do (
  echo %%f>> sources.txt
)
if not exist sources.txt (
  echo [ERREUR] Aucune source Java trouvee dans %SRC_DIR%
  pause
  exit /b 1
)

javac -encoding UTF-8 -cp "%SERVLET_API_JAR%" -d "%BUILD_DIR%" @sources.txt
if errorlevel 1 (
  echo La compilation a echoue.
  pause
  exit /b 1
)

echo [2/4] Creation du JAR...
jar cf "%DIST_DIR%\myframework.jar" -C "%BUILD_DIR%" .
if errorlevel 1 (
  echo Echec de creation du JAR.
  pause
  exit /b 1
)

echo [3/4] Copie du JAR dans l'appli de test...
copy /Y "%DIST_DIR%\myframework.jar" "test\WEB-INF\lib\myframework.jar" >nul
if errorlevel 1 (
  echo Echec de la copie du JAR vers test\WEB-INF\lib
  pause
  exit /b 1
)

echo [4/4] Deploiement de l'appli test vers Tomcat...
rem Suppression de l'ancienne webapp si elle existe
if exist "%TOMCAT_WEBAPPS%\test" (
  echo Suppression de l'ancienne webapp test...
  rd /s /q "%TOMCAT_WEBAPPS%\test"
)

rem Copie du dossier test vers Tomcat webapps
echo Copie du dossier test vers %TOMCAT_WEBAPPS%\test...
xcopy /E /I "test" "%TOMCAT_WEBAPPS%\test"
if errorlevel 1 (
  echo Echec du deploiement vers Tomcat webapps
  pause
  exit /b 1
)

echo.
echo === DEPLOIEMENT TERMINE ===
echo L'application est maintenant deployee dans Tomcat.
echo Prochaines etapes:
echo  1. Demarrer Tomcat: %TOMCAT_DIR%\bin\startup.bat
echo  2. Tester routes: http://localhost:8080/test/  et  http://localhost:8080/test/hello  et  http://localhost:8080/test/time
echo  3. Si une route n'existe pas, la liste des URLs connues s'affiche.

echo.
pause
endlocal

