@echo off

echo == Launching the graphical administration tool ==

call setHome

set JORAM_LIBS=%JORAM_HOME%\lib

set CLASSPATH=%JORAM_LIBS%\joramgui.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\joram.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\JCup.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jms.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jndi.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\log4j.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\ow_monolog.jar

%JAVA_HOME%\bin\java org.objectweb.joram.client.tools.admin.AdminTool
