#!/bin/sh

if [%1]==[]  goto no_arg

if not [%2]==[]  goto too_many_args

echo == Launching the $1 client ==

. setHome.sh

JORAM_LIBS=$JORAM_HOME/lib
SAMPLE_CLASSES=$JORAM_HOME/samples/classes/joram
CONFIG_ENV=$JORAM_HOME/samples/config

CLASSPATH=$JORAM_LIBS/jms.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jndi.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/log4j.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$SAMPLE_CLASSES
CLASSPATH=$CLASSPATH:$CONFIG_ENV

$JAVA_HOME/bin/java $1
goto :EOF

:no_arg
echo !! Missing file argument !!
goto :EOF

:too_many_args
echo !! Too many arguments !!
goto :EOF
