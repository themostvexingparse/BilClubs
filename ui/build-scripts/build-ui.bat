@echo off
setlocal EnableDelayedExpansion

:: ── Configure these two paths for your machine ──────────────────────────────
set JAVAFX=C:\Users\nazli\Desktop\filesjavafx\javafx-sdk-25.0.2\lib
:: ─────────────────────────────────────────────────────────────────────────────

:: Resolve all paths relative to this script, not the working directory
set ROOT=%~dp0..
set SRC=%ROOT%\src
set BIN=%ROOT%\bin
set RES=%ROOT%\resources
set LIB=%ROOT%\lib

echo [1/3] Cleaning bin...
if exist "%BIN%" rmdir /s /q "%BIN%"
mkdir "%BIN%"

echo [2/3] Compiling Java sources...
set SOURCES=
for /r "%SRC%" %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
javac --module-path "%JAVAFX%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "%LIB%\*" ^
      -d "%BIN%" ^
      %SOURCES%

if %ERRORLEVEL% neq 0 (
    echo COMPILATION FAILED. Aborting.
    exit /b 1
)

echo [3/3] Copying resources...
xcopy /s /i /y "%RES%" "%BIN%"

echo.
echo Build successful.
endlocal