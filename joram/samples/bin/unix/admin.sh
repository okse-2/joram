#!/bin/sh

echo == Launching the graphical administration tool ==

. setHome.sh

JORAM_LIBS=$JORAM_HOME/lib
CONFIG_ENV=$JORAM_HOME/samples/config

CLASSPATH=$JORAM_LIBS/joramgui.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/joram.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/JCup.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jms.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/jndi.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/log4j.jar
CLASSPATH=$CLASSPATH:$JORAM_LIBS/ow_monolog.jar
CLASSPATH=$CLASSPATH:$CONFIG_ENV

$JAVA_HOME/bin/java org.objectweb.joram.client.tools.admin.AdminTool
