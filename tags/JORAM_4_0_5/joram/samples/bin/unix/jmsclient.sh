#!/bin/sh

if [ -z $1 ]
then
  echo "!! Missing file argument !!"
  exit 1
fi

if [ ! -z $2 ]
then
  echo "!! Too many arguments !!"
  exit 1
fi

echo "== Launching the $1 client =="

. setHome.sh

JORAM_LIBS=$JORAM_HOME/lib
SAMPLE_CLASSES=$JORAM_HOME/samples/classes/joram
CONFIG_ENV=$JORAM_HOME/samples/config

CLASSPATH=$JORAM_LIBS/jms.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jndi.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-client.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/log4j.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$SAMPLE_CLASSES
CLASSPATH=$CLASSPATH:$CONFIG_ENV

$JAVA_HOME/bin/java -classpath $CLASSPATH $1
