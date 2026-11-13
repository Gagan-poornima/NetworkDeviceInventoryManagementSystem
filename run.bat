@echo off
setlocal
cd /d "%~dp0"

echo ===============================================================
echo Starting Network Device Inventory Management System...
echo ===============================================================

set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jre-1.8\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jre-1.8\bin\java.exe"
) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe"
)

"%JAVA_CMD%" -cp "bin;lib\mysql-connector-j-8.3.0.jar;src" com.inventory.Main
if errorlevel 1 (
    echo.
    echo An error occurred while running the application.
    pause
)

