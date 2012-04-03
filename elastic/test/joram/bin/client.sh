#!/bin/sh

# Definition of environment variables
JORAM_HOME=/home/ubuntu/joram
JAVA_HOME=/home/ubuntu/jdk1.7.0_02

if [ -z $1 ]
then
  echo "!! Missing class argument: compile samples and try 'classic.ClassicAdmin', 'classic.Sender' and 'classic.Receiver' for example !!"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/config
JORAM_BUNDLES=$JORAM_HOME/ship/bundle
RUN_DIR=$JORAM_HOME/run
ALIAS_CLASSES=$JORAM_HOME/classes/

# Building the Classpath
CLASSPATH=$JORAM_BUNDLES/a3-common.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-client.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/geronimo-jms_1.1_spec.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-client-jms.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jcup.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/monolog.jar
LASSPATH=$CLASSPATH:$JORAM_BUNDLES/sadt-monitoring-1.0.0-SNAPSHOT.jar
CLASSPATH=$CLASSPATH:$ALIAS_CLASSES
CLASSPATH=$CLASSPATH:$RUN_DIR

cp $CONFIG_DIR/jndi.properties $RUN_DIR/jndi.properties
cp $CONFIG_DIR/a3debug.cfg $RUN_DIR/a3debug.cfg

cd $RUN_DIR; exec "${JAVA_HOME}"/bin/java -classpath $CLASSPATH $*
