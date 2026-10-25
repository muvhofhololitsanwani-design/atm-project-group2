@echo off
REM Run the robot-style GUI test (opens real windows for about 15 seconds).
cd /d "%~dp0.."
if not exist out mkdir out
if not exist docs\screenshots mkdir docs\screenshots
javac -d out src\atm\*.java test\atm\*.java
if errorlevel 1 exit /b 1
java -cp out atm.GuiWalkthrough
