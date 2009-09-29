#!/bin/sh

# CYGWIN specific support.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JORAM_HOME" ] && JORAM_HOME=`cygpath --unix "$JORAM_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

echo $JORAM_HOME

# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/samples/bin/admin.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Verify if JAVA_HOME is well defined
if [ ! -r "$JAVA_HOME"/bin/java ]; then
  echo "The JAVA_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/samples/config
JORAM_LIBS=$JORAM_HOME/ship/lib
RUN_DIR=$JORAM_HOME/samples/run

# Building the Classpath
CLASSPATH=$JORAM_LIBS/joram-client.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-mom.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$RUN_DIR

mkdir $RUN_DIR
cp $CONFIG_DIR/a3config.dtd $RUN_DIR/a3config.dtd
cp $CONFIG_DIR/a3debug.cfg $RUN_DIR/a3debug.cfg
cp $CONFIG_DIR/centralized_a3servers.xml $RUN_DIR/a3servers.xml
cp $CONFIG_DIR/jndi.properties $RUN_DIR/jndi.properties

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  JORAM_HOME=`cygpath --path --windows "$JORAM_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo $CLASSPATH

echo "== Launching a non persistent server#0 =="
cd $RUN_DIR; exec "${JAVA_HOME}"/bin/java -Dcom.sun.management.jmxremote -DMXServer=com.scalagent.jmx.JMXServer -classpath $CLASSPATH fr.dyade.aaa.agent.AgentServer 0 ./s0
