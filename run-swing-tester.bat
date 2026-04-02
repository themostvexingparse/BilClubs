@echo off
echo [*] Cleaning environment...
call kill-server.bat

echo [*] Compiling server...
pushd server
javac -cp ".;lib/*" *.java -encoding UTF-8 

echo [*] Starting server in background...
start "BilClubsServer" /D "%~dp0server" /MIN cmd /c java -cp ".;lib/*" BilClubsServer -encoding UTF-8 ^> server.log 2^>^&1
popd

echo [*] Waiting for server to initialize...
ping 127.0.0.1 -n 4 > nul

if not exist "tests" mkdir tests

echo [*] Compiling Swing API Tester...
pushd tests
javac -cp ".;..\server\lib\*" *.java -encoding UTF-8
if errorlevel 1 (
    echo Compilation failed!
    popd
    pause
    exit /b 1
)

echo [*] Launching Swing API Tester...
java -cp ".;..\server\lib\*" BilClubsTestSuite
popd

echo [*] Tests complete. Cleaning up...
call kill-server.bat
echo [*] Done.
