@echo off
chcp 65001 >nul 2>nul

echo ====================================
echo   Starting backend and frontend...
echo ====================================
echo.

echo [1/2] Starting Spring Boot backend...
start "Backend" /D "%~dp0" mvnw.cmd spring-boot:run

timeout /t 5 /nobreak >nul 2>nul

echo [2/2] Starting Vue frontend dev server...
start "Frontend" /D "%~dp0UI" npm run dev

echo.
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo.
pause
