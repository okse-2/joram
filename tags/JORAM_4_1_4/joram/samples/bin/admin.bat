@echo off
REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\samples\bin\admin.bat" goto nokHome
REM Verify if JAVA_HOME is well defined
if not exist "%JAVA_HOME%\bin\java.exe" goto nokJava

set CONFIG_DIR=%JORAM_HOME%\samples\config
set JORAM_LIBS=%JORAM_HOME%\ship\lib
set RUN_DIR=%JORAM_HOME%\samples\run

REM  Building the Classpath
set CLASSPATH=%JORAM_LIBS%\joram-gui.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\joram-client.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\joram-shared.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\JCup.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jms.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jndi.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\ow_monolog.jar
set CLASSPATH=%CLASSPATH%;%RUN_DIR%

mkdir %RUN_DIR%
copy %CONFIG_DIR%\a3debug.cfg %RUN_DIR%\a3debug.cfg
copy %CONFIG_DIR%\jndi.properties %RUN_DIR%\jndi.properties

echo == Launching the graphical administration tool ==
start /D %RUN_DIR% /B %JAVA_HOME%\bin\java -classpath %CLASSPATH% org.objectweb.joram.client.tools.admin.AdminTool
goto end
:nokHome
echo The JORAM_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:nokJava
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:end