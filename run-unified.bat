@echo off
setlocal

echo === Building UI ===
call "%~dp0ui\build-scripts\build-ui.bat"
if %ERRORLEVEL% neq 0 (
    echo Build failed. Aborting.
    exit /b 1
)

echo === Starting Server ===
start "" "%~dp0run.bat"

echo Waiting for server to start...
timeout /t 3 /nobreak

echo === Running UI ===
call "%~dp0ui\build-scripts\run-ui.bat"

endlocal