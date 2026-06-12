# start-motus.ps1 - Lance tout le projet Motus
# Compatible PowerShell 5.1+

$BASE = Split-Path -Parent $MyInvocation.MyCommand.Path

function StartService($name, $port, $dir) {
    $bat = "$env:TEMP\motus-$port.bat"
    $lines = "@echo off", "title [$port] $name", "cd /d `"$dir`"", "mvn spring-boot:run", "pause"
    [System.IO.File]::WriteAllLines($bat, $lines, [System.Text.Encoding]::ASCII)
    Start-Process "cmd" -ArgumentList "/k `"$bat`""
}

Clear-Host
Write-Host ""
Write-Host "  ===== MOTUS Microservices =====" -ForegroundColor Red
Write-Host ""

# 1. Docker
Write-Host ""
Write-Host "  [1/5] Demarrage des bases de donnees (Docker)..." -ForegroundColor Cyan
docker-compose -f "$BASE\docker-compose.dev.yml" up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ERREUR: Docker n'est pas lance. Ouvre Docker Desktop et reessaie." -ForegroundColor Red
    Read-Host "Appuie sur Entree pour quitter"
    exit 1
}
Write-Host "  [OK] Bases de donnees demarrees" -ForegroundColor Green
Start-Sleep -Seconds 5

# 2. dictionnaire-service
Write-Host ""
Write-Host "  [2/5] dictionnaire-service (port 8082)..." -ForegroundColor Cyan
StartService "dictionnaire-service" "8082" "$BASE\dictionnaire-service"

# 3. joueur-service
Write-Host ""
Write-Host "  [3/5] joueur-service (port 8081)..." -ForegroundColor Cyan
StartService "joueur-service" "8081" "$BASE\joueur-service"

Write-Host ""
Write-Host "  Attente 45s - Spring Boot demarre..." -ForegroundColor Yellow
Write-Host "  (regarde les fenetres cmd ouvertes)" -ForegroundColor Gray
Start-Sleep -Seconds 45

# 4. partie-service
Write-Host ""
Write-Host "  [4/5] partie-service (port 8083)..." -ForegroundColor Cyan
StartService "partie-service" "8083" "$BASE\partie-service"
Start-Sleep -Seconds 30

# 5. statistiques-service
Write-Host ""
Write-Host "  [5/5] statistiques-service (port 8084)..." -ForegroundColor Cyan
StartService "statistiques-service" "8084" "$BASE\statistiques-service"

# Resume
Write-Host ""
Write-Host "  =============================================" -ForegroundColor Green
Write-Host "   Tout est lance !" -ForegroundColor Green
Write-Host "  =============================================" -ForegroundColor Green
Write-Host ""
Write-Host "   joueur-service       -> http://localhost:8081"
Write-Host "   dictionnaire-service -> http://localhost:8082"
Write-Host "   partie-service       -> http://localhost:8083"
Write-Host "   statistiques-service -> http://localhost:8084"
Write-Host ""
Write-Host "   Frontend : ouvre frontend/index.html dans ton navigateur" -ForegroundColor Cyan
Write-Host ""
Read-Host "Appuie sur Entree pour fermer cette fenetre"
