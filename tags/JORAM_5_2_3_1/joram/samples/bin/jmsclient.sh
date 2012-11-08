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

# Test the argument number
if [ -z $1 ]
then
  echo "!! Missing class argument !!"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/samples/config
JORAM_LIBS=$JORAM_HOME/ship/lib
RUN_DIR=$JORAM_HOME/samples/run
SAMPLE_CLASSES=$JORAM_HOME/samples/classes/joram

# Verify if RUN_DIR is correctly installed
if [ ! -r "$RUN_DIR"/a3servers.xml ]; then
  echo "You must first launch servers to create run directory."
  exit 1
fi

# Building the Classpath
CLASSPATH=$JORAM_LIBS/joram-client.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-mom.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jakarta-regexp-1.2.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jms.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jndi.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$SAMPLE_CLASSES
CLASSPATH=$CLASSPATH:$RUN_DIR

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  JORAM_HOME=`cygpath --path --windows "$JORAM_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo "== Launching the $1 client =="
cd $RUN_DIR; exec "${JAVA_HOME}"/bin/java -classpath $CLASSPATH $*
