REM Vtest script that launches JORAM test
REM uncomment following line to disable output
REM @echo off

set SERVER_RUN_DIR=%VTEST_HOME%\joram\src

echo == Launching JORAM test via ant tests.jms.all ==
start /D %SERVER_RUN_DIR% ant tests.jms.all
REM ant vtest.check.reports
