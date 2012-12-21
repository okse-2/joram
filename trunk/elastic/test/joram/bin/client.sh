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
CLASSES=$JORAM_HOME/classes/

# Building the Classpath
CLASSPATH=$JORAM_BUNDLES/a3-common.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-client.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/geronimo-jms_1.1_spec.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-client-jms.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jcup.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/monolog.jar
CLASSPATH=$CLASSPATH:$CLASSES
CLASSPATH=$CLASSPATH:$RUN_DIR

#AWS
AWS_JARS=$JORAM_HOME/ship/aws
CLASSPATH=$CLASSPATH:$AWS_JARS/aws-java-sdk-1.3.8.jar
CLASSPATH=$CLASSPATH:$AWS_JARS/commons-codec-1.3.jar
CLASSPATH=$CLASSPATH:$AWS_JARS/commons-logging-1.1.1.jar
CLASSPATH=$CLASSPATH:$AWS_JARS/httpclient-4.1.1.jar
CLASSPATH=$CLASSPATH:$AWS_JARS/httpcore-4.1.jar


cp $CONFIG_DIR/jndi.properties $RUN_DIR/jndi.properties
cp $CONFIG_DIR/a3debug.cfg $RUN_DIR/a3debug.cfg

cp $CONFIG_DIR/elasticity.properties $RUN_DIR/elasticity.properties

cd $RUN_DIR; exec "${JAVA_HOME}"/bin/java -classpath $CLASSPATH $*
