@echo off
REM Compile and start the ATM (Windows). Needs JDK 17 or newer.
cd /d "%~dp0.."
if not exist out mkdir out
javac -d out src\atm\*.java
if errorlevel 1 exit /b 1
java -cp out atm.ATMApp
