@echo off
setlocal
set JAVAFX=C:\javafx-sdk-17.0.18\lib
set ROOT=%~dp0..

java --module-path "%JAVAFX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "%ROOT%\bin;%ROOT%\lib\*" ^
     bilclubs.App
endlocal