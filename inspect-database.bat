@echo off
call kill-server.bat
pushd server
java -jar lib/explorer-2.9.5.jar
popd
