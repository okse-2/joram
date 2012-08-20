#!/bin/bash
#################################
# Vtest linux script for JORAM  #
#################################

#ensure existence of ENV variable VTEST_HOME
if [ -z "$VTEST_HOME" ]
then
    echo "variable VTEST_HOME doesn't exist"
    echo "please set env variable to the Vtest \"runtest\" script path"
    exit 1
else
    echo "VTEST_HOME set to $VTEST_HOME"
fi

cd $VTEST_HOME;
mvn install;
cd src;
ant tests.all;
mkdir results;
echo "OK" > results/report.txt;
zip results.zip results;

echo "joram JMS tests are finished";
exit 0;
