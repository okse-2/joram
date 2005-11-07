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

# Verify if JAVA_HOME is well defined
if [ ! -r "$JAVA_HOME"/bin/java ]; then
  echo "The JAVA_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/bin/raconfig.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

JORAM_LIBS=$JORAM_HOME/lib

# Building the Classpath
CLASSPATH=$JORAM_LIBS/joram-raconfig.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo "== run: java RAConfig $* =="
echo 
$JAVA_HOME/bin/java -classpath $CLASSPATH org.objectweb.joram.client.connector.utils.RAConfig $*
