@echo off
setlocal
cd /d "%~dp0application\application"
set "SPRING_PROFILES_ACTIVE="
call gradlew.bat bootRun

pause