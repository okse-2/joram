@echo off
REM treating number of parameter
if [%1]==[] goto usage
if not [%2]==[] goto usage

if "%1"=="6" (
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35
echo using jdk 6
goto test
) else if "%1"=="7" ( 
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_09
echo using jdk 7
goto test
) else (
echo %1 is a value not supported
goto usage
)

:usage
echo runtest.bat takes exatcly one parameter (jdk number : 6 or 7)
goto end

:test
set JAVA=%JAVA_HOME%\bin
set PATH=%JAVA_HOME%\bin;%PATH%
set RUN_DIR=C:\vtest\joram-test\src

echo == java version  ==
java -version >> batrun.txt

echo == Launching ant test  ==
start /D %RUN_DIR% ant tests.jms.all -Dship.dir=..\..\..\joram-src\ship >> batrun.txt

:end
