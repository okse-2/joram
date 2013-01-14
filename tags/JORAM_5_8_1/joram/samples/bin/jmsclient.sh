#!/bin/sh
# Copyright (C) 2000 - 2012 ScalAgent Distributed Technologies
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
# USA.

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
if [ ! -r "$JORAM_HOME"/samples/bin/clean.sh ]; then
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
  echo "!! Missing class argument: compile samples and try 'classic.ClassicAdmin', 'classic.Sender' and 'classic.Receiver' for example !!"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/samples/config
JORAM_BUNDLES=$JORAM_HOME/ship/bundle
RUN_DIR=$JORAM_HOME/samples/run
SAMPLE_CLASSES=$JORAM_HOME/samples/classes/joram

# Verify if RUN_DIR is correctly installed
if [ ! -d $RUN_DIR ]; then
  echo "You must first launch servers to create run directory."
  exit 1
fi

cp $CONFIG_DIR/a3debug.cfg $RUN_DIR/a3debug.cfg
cp $CONFIG_DIR/jndi.properties $RUN_DIR/jndi.properties

# Building the Classpath
CLASSPATH=$JORAM_BUNDLES/a3-common.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-client.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jndi-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/geronimo-jms_1.1_spec.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-client-jms.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/joram-shared.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/jcup.jar
CLASSPATH=$CLASSPATH:$JORAM_BUNDLES/monolog.jar
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
