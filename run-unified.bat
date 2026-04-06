@echo off
setlocal
echo === Starting Server ===
start "" "%~dp0run.bat"

echo Waiting for server to start...
timeout /t 3 /nobreak


echo === Building UI ===
call "%~dp0ui\build-scripts\unified.bat"
if %ERRORLEVEL% neq 0 (
    echo Build failed. Aborting.
    exit /b 1
)

endlocal