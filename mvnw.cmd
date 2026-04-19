@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
IF "%BASE_DIR:~-1%"=="\" SET BASE_DIR=%BASE_DIR:~0,-1%
IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_EXEC=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_EXEC=java
)

"%JAVA_EXEC%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -cp "%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
