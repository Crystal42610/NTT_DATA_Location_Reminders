@echo off
setlocal
cd /d "%~dp0.."
echo [1/3] Build debug APK...
call gradlew.bat :app:assembleDebug --stacktrace
if errorlevel 1 exit /b %errorlevel%
echo [2/3] Run JVM tests...
call gradlew.bat :app:testDebugUnitTest
if errorlevel 1 exit /b %errorlevel%
echo [3/3] Run static analysis...
call gradlew.bat :app:lintDebug
if errorlevel 1 exit /b %errorlevel%
echo Build, unit tests and lint passed. Run device tests and geofence scenarios in README.
endlocal
