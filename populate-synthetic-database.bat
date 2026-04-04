@echo off
echo ═══════════════════════════════════════════════════════════════
echo   BilClubs — Synthetic Database Populator
echo ═══════════════════════════════════════════════════════════════
echo.

REM ── Collect admin credentials ────────────────────────────────
set /p ADMIN_EMAIL="Admin email: "
set /p ADMIN_PASS="Admin password: "
echo.

REM ── Make sure the server is compiled and running ─────────────
echo [*] Compiling server...
pushd server
javac -cp ".;lib/*" *.java -encoding UTF-8
if errorlevel 1 (
    echo [!] Server compilation failed!
    popd
    pause
    exit /b 1
)

echo [*] Starting server in background...
start "BilClubsServer" /D "%~dp0server" /MIN cmd /c java -cp ".;lib/*" BilClubsServer -encoding UTF-8 ^> server.log 2^>^&1
popd

echo [*] Waiting for server to initialize...
ping 127.0.0.1 -n 4 > nul

REM ── Compile and run the populator ────────────────────────────
echo [*] Compiling SyntheticDatabaseTest...
pushd tests
javac -cp ".;..\server\lib\*" SyntheticDatabaseTest.java RequestManager.java Response.java StreamReader.java ServerConfig.java -encoding UTF-8
if errorlevel 1 (
    echo [!] Compilation failed!
    popd
    pause
    exit /b 1
)

echo [*] Running SyntheticDatabaseTest...
echo.
java -cp ".;..\server\lib\*" SyntheticDatabaseTest "%ADMIN_EMAIL%" "%ADMIN_PASS%"
popd

echo.
echo [*] Shutting down server...
call kill-server.bat
echo [*] Done.
pause
