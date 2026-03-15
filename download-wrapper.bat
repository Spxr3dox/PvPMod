@echo off
echo Downloading Gradle wrapper...
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/releases/download/v8.12.0/gradle-8.12.0-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'" 2>nul
powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.12-bin.zip' -OutFile '%USERPROFILE%\.gradle\wrapper\dists\gradle-8.12-bin.zip'" 2>nul
echo Done.
