@echo off

if [%1]==[]  goto no_arg

if not [%2]==[]  goto too_many_args

echo == Launching the %1 client ==

call setHome

set JORAM_LIBS=%JORAM_HOME%\lib
set SAMPLE_CLASSES=%JORAM_HOME%\samples\classes\joram
set CONFIG_ENV=%JORAM_HOME%\samples\config

set CLASSPATH=%JORAM_LIBS%\jms.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\jndi.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\joram-client.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\joram-shared.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\JCup.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\log4j.jar
set CLASSPATH=%CLASSPATH%;%JORAM_LIBS%\ow_monolog.jar
set CLASSPATH=%CLASSPATH%;%SAMPLE_CLASSES%
set CLASSPATH=%CLASSPATH%;%CONFIG_ENV%

%JAVA_HOME%\bin\java -classpath %CLASSPATH% %1
goto :EOF

:no_arg
echo !! Missing file argument !!
goto :EOF

:too_many_args
echo !! Too many arguments !!
goto :EOF
