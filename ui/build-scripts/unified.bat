@echo off
setlocal EnableDelayedExpansion

set "JAVAFX="

echo [JavaFX] Auto-detecting SDK...

if defined JAVAFX_HOME (
    call :tryLib "%JAVAFX_HOME%\lib"
    if defined JAVAFX goto :detected
    call :tryLib "%JAVAFX_HOME%"
    if defined JAVAFX goto :detected
    echo [JavaFX] JAVAFX_HOME is set but no valid SDK found there: %JAVAFX_HOME%
)

for %%P in ("%PATH:;=" "%") do (
    call :tryLib "%%~P"
    if defined JAVAFX goto :detected
    call :tryLib "%%~dpP\lib"
    if defined JAVAFX goto :detected
)

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

if exist "%USERPROFILE%\scoop\apps" (
    for /d %%D in ("%USERPROFILE%\scoop\apps\*javafx*") do (
        call :tryLib "%%~D\current\lib"
        if defined JAVAFX goto :detected
        for /d %%V in ("%%~D\*") do (
            call :tryLib "%%~V\lib"
            if defined JAVAFX goto :detected
        )
    )
)

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

echo [JavaFX] Common locations exhausted — scanning drive roots (may take a moment)...
for %%R in ("%USERPROFILE%\Desktop" "C:\" "D:\") do (
    call :searchRoot %%R
    if defined JAVAFX goto :detected
)

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

set "ROOT=%~dp0.."
set "SRC=%ROOT%\src"
set "BIN=%ROOT%\bin"
set "RES=%ROOT%\resources"
set "LIB=%ROOT%\lib"

echo [1/4] Cleaning bin...
if exist "%BIN%" rmdir /s /q "%BIN%"
mkdir "%BIN%"

echo [2/4] Compiling Java sources...
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

echo [3/4] Copying resources...
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

:searchRoot
if not exist %1 goto :eof
for /d %%D in ("%~1\*javafx*") do (
    call :tryLib "%%~D\lib"
    if defined JAVAFX goto :eof
)
for /d %%D in ("%~1\*") do (
    for /d %%E in ("%%~D\*javafx*") do (
        call :tryLib "%%~E\lib"
        if defined JAVAFX goto :eof
    )
)
goto :eof

:tryLib
if "%~1"=="" goto :eof
if not exist "%~1\javafx.base.jar" goto :eof
set "JAVAFX=%~1"
goto :eof
