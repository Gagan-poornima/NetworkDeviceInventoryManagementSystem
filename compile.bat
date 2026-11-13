@echo off
setlocal
cd /d "%~dp0"

echo ===============================================================
echo Compiling Network Device Inventory Management System...
echo ===============================================================

if not exist "bin" mkdir "bin"

set "JAVAC_CMD=javac"
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\javac.exe"
)

"%JAVAC_CMD%" --release 8 -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar;src" -d bin src\com\inventory\model\*.java src\com\inventory\db\*.java src\com\inventory\dao\*.java src\com\inventory\ui\*.java src\com\inventory\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Compilation completed successfully!
) else (
    echo.
    echo [ERROR] Compilation failed!
    pause
)

