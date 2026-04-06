@echo off
setlocal
set JAVAFX=C:\Users\nazli\Desktop\filesjavafx\javafx-sdk-25.0.2\lib
set ROOT=%~dp0..

java --module-path "%JAVAFX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "%ROOT%\bin;%ROOT%\lib\*" ^
     bilclubs.App
endlocal