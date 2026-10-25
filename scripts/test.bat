@echo off
REM Compile everything and run the backend test suite (Windows).
cd /d "%~dp0.."
if not exist out mkdir out
javac -d out src\atm\*.java test\atm\BackendTests.java
if errorlevel 1 exit /b 1
java -cp out atm.BackendTests
