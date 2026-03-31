cmd /c clean
rmdir /s /q db
javac -cp ".;lib/*" *.java -encoding UTF-8 
cls
java -cp ".;lib/*" BilClubsServer -encoding UTF-8 