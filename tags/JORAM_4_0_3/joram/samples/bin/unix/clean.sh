#!/bin/sh

echo "== Cleaning the persistence directories and configuration settings =="

. setHome.sh

CONFIG_HOME=$JORAM_HOME/samples/config
RUN_DIR=$JORAM_HOME/samples/run

rm $CONFIG_HOME/a3servers.xml
rm $CONFIG_HOME/jndi.properties

rm -rf $RUN_DIR
