#!/bin/sh

echo "== Launching a persistent server s0 =="

. setHome.sh

JORAM_LIBS=$JORAM_HOME/lib
CONFIG_HOME=$JORAM_HOME/samples/config
RUN_DIR=$JORAM_HOME/samples/run

# Building the Classpath
CLASSPATH=$JORAM_LIBS/mom.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/xerces.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jakarta-regexp-1.2.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/log4j.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$CONFIG_HOME

cp $CONFIG_HOME/distributed_a3servers.xml $CONFIG_HOME/a3servers.xml
cp $CONFIG_HOME/tcp_jndi.properties $CONFIG_HOME/jndi.properties

if [ ! -d $JORAM_HOME/samples/run ]
then
  mkdir $RUN_DIR
fi
cd $RUN_DIR

$JAVA_HOME/bin/java -classpath $CLASSPATH -DTransaction=fr.dyade.aaa.util.ATransaction fr.dyade.aaa.agent.AgentServer 0 ./s0
