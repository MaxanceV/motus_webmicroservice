@echo off
setlocal
set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

echo.
echo  ===== MOTUS Microservices =====
echo.

REM 1. Docker
echo  [1/5] Demarrage des bases de donnees (Docker)...
docker-compose -f "%BASE%\docker-compose.dev.yml" up -d
if errorlevel 1 (
    echo  ERREUR: Docker n'est pas lance. Ouvre Docker Desktop et reessaie.
    pause
    exit /b 1
)
echo  [OK] Bases de donnees demarrees
timeout /t 5 /nobreak >nul

REM 2. dictionnaire-service
echo  [2/5] dictionnaire-service (port 8082)...
start "[8082] dictionnaire-service" /d "%BASE%\dictionnaire-service" cmd /k "mvn spring-boot:run"

REM 3. joueur-service
echo  [3/5] joueur-service (port 8081)...
start "[8081] joueur-service" /d "%BASE%\joueur-service" cmd /k "mvn spring-boot:run"

echo  Attente 45s - Spring Boot demarre...
timeout /t 45 /nobreak >nul

REM 4. partie-service
echo  [4/5] partie-service (port 8083)...
start "[8083] partie-service" /d "%BASE%\partie-service" cmd /k "mvn spring-boot:run"
timeout /t 30 /nobreak >nul

REM 5. statistiques-service
echo  [5/5] statistiques-service (port 8084)...
start "[8084] statistiques-service" /d "%BASE%\statistiques-service" cmd /k "mvn spring-boot:run"

echo.
echo  =============================================
echo   Tout est lance !
echo  =============================================
echo.
echo   joueur-service       -^> http://localhost:8081
echo   dictionnaire-service -^> http://localhost:8082
echo   partie-service       -^> http://localhost:8083
echo   statistiques-service -^> http://localhost:8084
echo.
echo   Frontend : ouvre frontend/index.html dans ton navigateur
echo.
pause
