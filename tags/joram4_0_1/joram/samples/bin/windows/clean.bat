@echo off

echo == Cleaning the persistence directories and configuration settings ==

call setHome

set CONFIG_HOME=%JORAM_HOME%\samples\config
set RUN_DIR=%JORAM_HOME%\samples\run

rm %CONFIG_HOME%\a3servers.xml
rm %CONFIG_HOME%\jndi.properties

rmdir /s /q %RUN_DIR%