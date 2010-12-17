@echo off
REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\samples\bin\clean.bat" goto nokHome
REM Verify if JAVA_HOME is well defined
if not exist "%JAVA_HOME%\bin\java.exe" goto nokJava
REM Test the argument number
if [%1]==[] goto no_arg
if not [%2]==[]  goto too_many_args

set CONFIG_DIR=%JORAM_HOME%\samples\config
set JORAM_BUNDLES=%JORAM_HOME%\ship\bundle
set RUN_DIR=%JORAM_HOME%\samples\run
set SAMPLE_CLASSES=%JORAM_HOME%\samples\classes\joram

if not exist "%RUN_DIR%" goto nokRunDir

cp %CONFIG_DIR%\a3debug.cfg %RUN_DIR%\a3debug.cfg
cp %CONFIG_DIR%\jndi.properties %RUN_DIR%\jndi.properties

REM  Building the Classpath
set CLASSPATH=%JORAM_BUNDLES%\a3-common.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\jndi-client.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\jndi-shared.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\geronimo-jms_1.1_spec.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\joram-client-jms.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\joram-shared.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\jcup.jar
set CLASSPATH=%CLASSPATH%;%JORAM_BUNDLES%\monolog.jar
set CLASSPATH=%CLASSPATH%;%SAMPLE_CLASSES%
set CLASSPATH=%CLASSPATH%;%RUN_DIR%

set PATH=%JAVA_HOME%\bin;%PATH%

echo == Launching the %1 client ==
start /D %RUN_DIR% /B java -classpath %CLASSPATH% %1
goto end
:nokHome
echo The JORAM_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:nokJava
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:nokRunDir
echo You must first launch servers to create run directory.
goto end
:no_arg
echo !! Missing classname argument: try 'classic.ClassicAdmin', 'classic.Sender' and 'classic.Receiver' for example !!
goto usage
:too_many_args
echo !! Too many arguments !!
goto usage
:usage
echo jmsclient <classname>
:end
