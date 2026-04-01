@echo off

echo [*] Cleaning logs...
if exist "server\log" rmdir /s /q "server\log"
if exist "server\logs" rmdir /s /q "server\logs"
del /s /q "server\*.log" >nul 2>&1

echo [*] Cleaning database files...
if exist "server\db\*.odb$" del /q /f "server\db\*.odb$" >nul 2>&1
rmdir /s /q server\db

echo [*] Workspace successfully cleaned.
