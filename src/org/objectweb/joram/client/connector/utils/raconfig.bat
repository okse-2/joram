@echo off
REM Verify if JAVA_HOME is well defined
if not exist "%JAVA_HOME%\bin\java.exe" goto nokJava

REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\bin\raconfig.bat" goto nokHome


set JORAM_LIBS=%JORAM_HOME%\lib

REM  Building the Classpath
set CLASSPATH=%JORAM_LIBS%\joram-raconfig.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\ow_monolog.jar

echo == Run: java RAConfig %1 %2 %3 %4 %5 %6 %7 %8 %9 ==
start /B %JAVA_HOME%\bin\java -classpath %CLASSPATH% org.objectweb.joram.client.connector.utils.RAConfig %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:nokJava
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:nokHome
echo The JORAM_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:end
