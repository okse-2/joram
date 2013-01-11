#!/bin/bash

# Definition of environment variables
JORAM_HOME=/home/ubuntu/joram
JAVA_HOME=/home/ubuntu/jdk1.7.0_10

# Test the argument number
if [ -z $1 ]
then
  echo "!! Missing server id argument (0, 1 or 2) - see a3servers.xml !!"
  exit 1
fi

if [ ! -z $2 ]
then
  echo "!! Too many arguments !!"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/config
JORAM_BIN=$JORAM_HOME/ship/bin
RUN_DIR=$JORAM_HOME/run
SERVER_RUN_DIR=$RUN_DIR/server$1

# Building the Classpath
CLASSPATH=$CLASSPATH:$JORAM_BIN/felix.jar

mkdir $RUN_DIR
mkdir $SERVER_RUN_DIR

# Clean $SERVER_RUN_DIR
rm -rf $SERVER_RUN_DIR/s$1 $SERVER_RUN_DIR/felix-cache

cp $CONFIG_DIR/a3config.dtd $SERVER_RUN_DIR/a3config.dtd
cp $CONFIG_DIR/a3debug.cfg $SERVER_RUN_DIR/a3debug.cfg
cp $CONFIG_DIR/a3servers.xml $SERVER_RUN_DIR/a3servers.xml
cp $CONFIG_DIR/config.properties $SERVER_RUN_DIR/config.properties

sed -e "s/NUM/$1/g" -e "s/RAND/$RANDOM/g" $CONFIG_DIR/MonitoringConfig.template > $SERVER_RUN_DIR/MonitoringConfig.xml

PORT=$((16500+$1))
cd $SERVER_RUN_DIR; exec "${JAVA_HOME}"/bin/java  -Dosgi.shell.telnet.port=$PORT -Dfelix.config.properties=file:config.properties -Dcom.sun.management.jmxremote -Dfr.dyade.aaa.agent.AgentServer.id=$1 -classpath $CLASSPATH org.apache.felix.main.Main
