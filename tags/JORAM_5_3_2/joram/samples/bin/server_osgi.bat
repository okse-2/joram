@echo off
REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\samples\bin\clean.bat" goto nokHome
REM Verify if JAVA_HOME is well defined
if not exist "%JAVA_HOME%\bin\java.exe" goto nokJava

set CONFIG_DIR=%JORAM_HOME%\samples\config
set JORAM_BIN=%JORAM_HOME%\ship\bin
set RUN_DIR=%JORAM_HOME%\samples\run

echo /!\ Warning: Don't forget server id when launching the script.

mkdir %RUN_DIR%
copy %CONFIG_DIR%\a3config.dtd %RUN_DIR%\a3config.dtd
copy %CONFIG_DIR%\a3debug.cfg %RUN_DIR%\a3debug.cfg
copy %CONFIG_DIR%\distributed_a3servers.xml %RUN_DIR%\a3servers.xml
copy %CONFIG_DIR%\jndi.properties %RUN_DIR%\jndi.properties
copy %CONFIG_DIR%\config.properties %RUN_DIR%\config.properties

set PATH=%JAVA_HOME%\bin;%PATH%

echo == Launching a persistent server#%1 ==
start /D %RUN_DIR% java -Dosgi.shell.telnet.port=1600%1 -Dfelix.config.properties=file:config.properties -Dfelix.cache.rootdir=./s%1 -Dcom.sun.management.jmxremote -DMXServer=com.scalagent.jmx.JMXServer -Dfr.dyade.aaa.agent.AgentServer.id=%1 -jar %JORAM_BIN%\felix.jar
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
