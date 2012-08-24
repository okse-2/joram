#!/bin/bash
#################################
# Vtest linux script for JORAM  #
#################################

#ensure existence of environment variable VTEST_HOME
if [ -z "$VTEST_HOME" ]
then
    echo "variable VTEST_HOME doesn't exist"
    echo "please set variable VTEST_HOME to the Vtest \"runtest\" script path"
    exit 1
else
    echo "VTEST_HOME set to $VTEST_HOME"
fi

#following command suppose that svn check out has been made into path $VTEST_HOME/joram
cd $VTEST_HOME/joram;

#installing joram tests using maven
mvn install > mvn.log;

# launching tests
cd src;
echo "on launching ant custom.tests.vtest"
ant tests.jms.all > ant.log;

mkdir results;
echo "OK" > results/report.txt;
jar cf results.zip results;

echo " joram tests are finished";
exit 0;
