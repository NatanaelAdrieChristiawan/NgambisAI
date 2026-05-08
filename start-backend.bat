@echo off
REM ========================================
REM Ngambis.ai — Backend Startup Script (Windows)
REM ========================================
REM Loads environment variables from .env file
REM then starts the Spring Boot application.
REM
REM Usage: start-backend.bat
REM ========================================

echo.
echo ========================================
echo  Ngambis.ai - Backend Startup
echo ========================================
echo.

REM Load .env file
if not exist "%~dp0.env" (
    echo [ERROR] No .env file found!
    echo         Create one based on .env.example
    pause
    exit /b 1
)

echo [INFO] Loading environment from .env...
for /f "usebackq tokens=1,* delims==" %%A in ("%~dp0.env") do (
    REM Skip comments and empty lines
    echo %%A | findstr /r "^#" >nul 2>&1
    if errorlevel 1 (
        if not "%%A"=="" (
            set "%%A=%%B"
        )
    )
)
echo [OK] Environment loaded successfully
echo.

echo [INFO] Starting Ngambis.ai Backend...
echo         Database: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo         Server:   http://localhost:%SERVER_PORT%
echo.

REM Run Spring Boot with Maven Wrapper
cd /d "%~dp0"
call mvnw.cmd spring-boot:run
