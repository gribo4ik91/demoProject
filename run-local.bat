@echo off
setlocal
cd /d "%~dp0application\application"
if exist ".local" rmdir /s /q ".local"
set "SPRING_PROFILES_ACTIVE=local"
call gradlew.bat bootRun

pause
