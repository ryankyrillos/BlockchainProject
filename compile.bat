@echo off
mkdir bin
javac -d bin -cp "lib/*" src/main/java/blockchain/*.java
echo Compilation complete!
