@echo off
REM Wrapper delegating to the Android app's gradle wrapper.
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set APP_WRAPPER=%SCRIPT_DIR%notes_app_frontend\gradlew.bat

if not exist "%APP_WRAPPER%" (
  echo Error: App gradle wrapper not found at "%APP_WRAPPER%"
  exit /b 127
)

call "%APP_WRAPPER%" %*
