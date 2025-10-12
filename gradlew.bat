@echo off
REM ------------------------------------------------------------------------------
REM Gradle startup script for Windows
REM ------------------------------------------------------------------------------

@REM Resolve the location of this script
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

REM Resolve any "." and ".." in APP_HOME to make it shorter
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

REM Default JVM options (can be overridden by JAVA_OPTS or GRADLE_OPTS)
set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%"=="0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto fail

:findJavaFromJavaHome
set "JAVA_HOME=%JAVA_HOME:"=%"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto fail

:execute
REM Setup the classpath to the Gradle wrapper jar
set "CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"

REM Execute the wrapper
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
exit /b 1

:mainEnd
endlocal
exit /b %ERRORLEVEL%
