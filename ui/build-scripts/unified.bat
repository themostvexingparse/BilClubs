@echo off
setlocal EnableDelayedExpansion

:: ═════════════════════════════════════════════════════════════════════════════
::  build-ui.bat  —  JavaFX auto-detecting build script
::
::  JavaFX SDK location is resolved automatically.  If detection fails, set
::  the JAVAFX_HOME environment variable and re-run:
::
::      setx JAVAFX_HOME "C:\path\to\javafx-sdk-XX"
::
::  Detection order:
::    1. JAVAFX_HOME environment variable
::    2. Entries already on PATH
::    3. Common filesystem locations (Desktop, Downloads, Program Files, …)
::    4. Scoop / Chocolatey package-manager install trees
::    5. Slow fallback: top-level folders on C:\ and D:\
:: ═════════════════════════════════════════════════════════════════════════════

set "JAVAFX="

echo [JavaFX] Auto-detecting SDK...

:: ─── 1. Explicit JAVAFX_HOME env var ────────────────────────────────────────
if defined JAVAFX_HOME (
    call :tryLib "%JAVAFX_HOME%\lib"
    if defined JAVAFX goto :detected
    call :tryLib "%JAVAFX_HOME%"
    if defined JAVAFX goto :detected
    echo [JavaFX] JAVAFX_HOME is set but no valid SDK found there: %JAVAFX_HOME%
)

:: ─── 2. Scan PATH entries ────────────────────────────────────────────────────
::  Developers sometimes add the lib\ dir directly to PATH.
for %%P in ("%PATH:;=" "%") do (
    call :tryLib "%%~P"
    if defined JAVAFX goto :detected
    :: Also try the sibling lib\ directory of any PATH entry
    call :tryLib "%%~dpP\lib"
    if defined JAVAFX goto :detected
)

:: ─── 3. Targeted filesystem search ──────────────────────────────────────────
::  For each root we look for *javafx* named subdirectories one and two levels
::  deep — fast because the glob is applied only at a known depth, not a full
::  recursive walk.

for %%R in (
    "%USERPROFILE%\Desktop"
    "%USERPROFILE%\Downloads"
    "%USERPROFILE%\Documents"
    "%USERPROFILE%"
    "%LOCALAPPDATA%"
    "%APPDATA%"
    "%ProgramFiles%"
    "%ProgramFiles(x86)%"
    "C:\Java"
    "C:\tools"
    "C:\dev"
    "C:\opt"
    "C:\sdk"
    "C:\Program Files\Java"
    "C:\Program Files\Eclipse Adoptium"
    "C:\Program Files\BellSoft"
    "C:\Program Files\Microsoft"
) do (
    call :searchRoot %%R
    if defined JAVAFX goto :detected
)

:: ─── 4. Package managers ─────────────────────────────────────────────────────

:: Scoop  (%USERPROFILE%\scoop\apps\<name>\current\)
if exist "%USERPROFILE%\scoop\apps" (
    for /d %%D in ("%USERPROFILE%\scoop\apps\*javafx*") do (
        call :tryLib "%%~D\current\lib"
        if defined JAVAFX goto :detected
        :: Some scoop manifests unpack the SDK one level further
        for /d %%V in ("%%~D\*") do (
            call :tryLib "%%~V\lib"
            if defined JAVAFX goto :detected
        )
    )
)

:: Chocolatey  (%ChocolateyInstall%\lib\<pkg>\tools\)
if defined ChocolateyInstall (
    if exist "%ChocolateyInstall%\lib" (
        for /d %%D in ("%ChocolateyInstall%\lib\*javafx*") do (
            call :tryLib "%%~D\lib"
            if defined JAVAFX goto :detected
            call :tryLib "%%~D\tools\lib"
            if defined JAVAFX goto :detected
            for /d %%E in ("%%~D\tools\*javafx*") do (
                call :tryLib "%%~E\lib"
                if defined JAVAFX goto :detected
            )
        )
    )
)

:: ─── 5. Slow fallback: drive roots ──────────────────────────────────────────
echo [JavaFX] Common locations exhausted — scanning drive roots (may take a moment)...
for %%R in ("%USERPROFILE%\Desktop" "C:\" "D:\") do (
    call :searchRoot %%R
    if defined JAVAFX goto :detected
)

:: ─── Nothing found ───────────────────────────────────────────────────────────
echo.
echo  ERROR: Could not locate a JavaFX SDK on this machine.
echo.
echo  To fix this, do ONE of the following:
echo.
echo    A) Set a permanent environment variable pointing to your SDK root:
echo         setx JAVAFX_HOME "C:\path\to\javafx-sdk-XX"
echo       Then open a new terminal and re-run this script.
echo.
echo    B) Download the JavaFX SDK from https://gluonhq.com/products/javafx/
echo       and extract it anywhere; then set JAVAFX_HOME as above.
echo.
echo    C) Add the SDK's lib\ directory to your PATH.
echo.
exit /b 1


:detected
echo [JavaFX] SDK lib found: %JAVAFX%
echo.

:: ═════════════════════════════════════════════════════════════════════════════
::  BUILD
:: ═════════════════════════════════════════════════════════════════════════════

:: Resolve all project paths relative to this script's directory
set "ROOT=%~dp0.."
set "SRC=%ROOT%\src"
set "BIN=%ROOT%\bin"
set "RES=%ROOT%\resources"
set "LIB=%ROOT%\lib"

echo [1/3] Cleaning bin...
if exist "%BIN%" rmdir /s /q "%BIN%"
mkdir "%BIN%"

echo [2/3] Compiling Java sources...
set "SOURCES="
for /r "%SRC%" %%f in (*.java) do set "SOURCES=!SOURCES! "%%f""

javac --module-path "%JAVAFX%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "%LIB%\*" ^
      -d "%BIN%" ^
      %SOURCES%

if %ERRORLEVEL% neq 0 (
    echo.
    echo  COMPILATION FAILED. Aborting.
    exit /b 1
)

echo [3/3] Copying resources...
xcopy /s /i /y "%RES%" "%BIN%"

echo.
echo  Build successful.

echo [4/4] Launching application...
java --module-path "%JAVAFX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "%BIN%;%LIB%\*" ^
     bilclubs.App

endlocal
goto :eof


:: ═════════════════════════════════════════════════════════════════════════════
::  SUBROUTINES
:: ═════════════════════════════════════════════════════════════════════════════

:: :searchRoot <root>
::   Looks for *javafx* subdirectories one and two levels under <root>.
:searchRoot
if not exist %1 goto :eof
:: One level deep
for /d %%D in ("%~1\*javafx*") do (
    call :tryLib "%%~D\lib"
    if defined JAVAFX goto :eof
)
:: Two levels deep  (e.g. C:\tools\sdks\javafx-sdk-21\)
for /d %%D in ("%~1\*") do (
    for /d %%E in ("%%~D\*javafx*") do (
        call :tryLib "%%~E\lib"
        if defined JAVAFX goto :eof
    )
)
goto :eof


:: :tryLib <path>
::   Sets JAVAFX if <path> contains javafx.base.jar  (the canonical SDK marker).
:tryLib
if "%~1"=="" goto :eof
if not exist "%~1\javafx.base.jar" goto :eof
set "JAVAFX=%~1"
goto :eof
