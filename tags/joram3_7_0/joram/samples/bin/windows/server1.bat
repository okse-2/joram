@echo off

echo == Launching a persistent server s1 ==

call setHome

set JORAM_LIBS=%JORAM_HOME%\lib
set CONFIG_HOME=%JORAM_HOME%\samples\config
set RUN_DIR=%JORAM_HOME%\samples\run

:: Building the Classpath
set CLASSPATH=%JORAM_LIBS%\mom.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\xerces.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\JCup.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jakarta-regexp-1.2.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\log4j.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\ow_monolog.jar
set CLASSPATH=%CLASSPATH%;%CONFIG_HOME%

cp %CONFIG_HOME%\distributed_a3servers.xml %CONFIG_HOME%\a3servers.xml
cp %CONFIG_HOME%\tcp_jndi.properties %CONFIG_HOME%\jndi.properties

mkdir %RUN_DIR%
cd %RUN_DIR%

%JAVA_HOME%\bin\java -DTransaction=fr.dyade.aaa.util.ATransaction fr.dyade.aaa.agent.AgentServer 1 ./s1
