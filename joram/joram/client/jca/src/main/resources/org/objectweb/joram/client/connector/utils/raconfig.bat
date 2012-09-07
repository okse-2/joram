REM Copyright (C) 2000 - 2012 ScalAgent Distributed Technologies
REM 
REM This library is free software; you can redistribute it and/or
REM modify it under the terms of the GNU Lesser General Public
REM License as published by the Free Software Foundation; either
REM version 2.1 of the License, or any later version.
REM 
REM This library is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
REM Lesser General Public License for more details.
REM 
REM You should have received a copy of the GNU Lesser General Public
REM License along with this library; if not, write to the Free Software
REM Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
REM USA.

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
