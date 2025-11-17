# Assessment Management Tool - Startup Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Assessment Management Tool" -ForegroundColor Cyan
Write-Host "Starting Application..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Checking prerequisites..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Java found" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java not found" -ForegroundColor Red
    pause
    exit 1
}

try {
    $nodeVersion = node --version
    Write-Host "[OK] Node.js found" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Node.js not found" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "[1/2] Starting Backend..." -ForegroundColor Yellow

$backendCmd = "Set-Location '$scriptDir'; .\mvnw.cmd spring-boot:run; pause"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd

Write-Host "[OK] Backend starting on http://localhost:8080" -ForegroundColor Green
Write-Host "Waiting 20 seconds for backend..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "[2/2] Starting Frontend..." -ForegroundColor Yellow

$webDir = Join-Path $scriptDir "web"
if (-not (Test-Path (Join-Path $webDir "node_modules"))) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    Push-Location $webDir
    npm install
    Pop-Location
}

$frontendCmd = "Set-Location '$webDir'; npm run dev; pause"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd

Write-Host "[OK] Frontend starting on http://localhost:5174" -ForegroundColor Green
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "APPLICATION STARTED!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Open: http://localhost:5174" -ForegroundColor Cyan
Write-Host "Login: admin@sheffield.ac.uk / admin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to close..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
