@echo off
title Set User Privilege
setlocal

echo ===================================================
echo   BilClubs -- Set User Privilege to 15
echo ===================================================
echo.

REM Run from project root. Server must NOT be running (ObjectDB file lock).

REM ── 1. Compile server (produces .class files in server/) ──────────────────
echo [*] Compiling server...
pushd server
javac -cp ".;lib/*" *.java embeddings\*.java -encoding UTF-8
if errorlevel 1 (
    echo [!] Server compilation failed.
    popd
    pause
    exit /b 1
)
popd

REM ── 2. Compile the tool, linking against server/ classes and its libs ─────
echo [*] Compiling tools\SetAdminPrivilege.java...
javac -cp "server;server\lib\*" -d tools tools\SetAdminPrivilege.java
if errorlevel 1 (
    echo [!] Tool compilation failed.
    pause
    exit /b 1
)

REM ── 3. Run from project root so db path "server/db/bilclubs.odb" resolves ─
echo [*] Running SetAdminPrivilege...
echo.
java -cp "server;server\lib\*;tools" SetAdminPrivilege

echo.
endlocal
pause
