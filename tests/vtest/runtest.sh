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

date=`date +%x`
date=`echo $date | sed -e s:/:.:g`
LOGFILE=$VTEST_HOME/"vtest-$date.log"
ZIPFILE=$VTEST_HOME/"result.zip"

#extracting joram source so as to test updated trunk artifacts
svn co svn://svn.forge.objectweb.org/svnroot/joram/trunk/joram $VTEST_HOME/joram-src
cd $VTEST_HOME/joram-src
echo "installing joram trunk" >> $LOGFILE
mvn install >> $LOGFILE


#following command suppose that svn check out has been made into path $VTEST_HOME/joram
cd $VTEST_HOME/joram;

#installing joram tests using maven
echo "installing joram tests" >> $LOGFILE
mvn install >> $LOGFILE;

# launching tests
cd src;
echo "on launching ant custom.tests.vtest"
ant custom.tests.vtest >> $LOGFILE;
ant vtest.check.reports >> $LOGFILE;
TEST_RESULT=$?;

mkdir results;
cp $LOGFILE results;

# getting test exit code
if [[ $TEST_RESULT -gt 0 ]]; then
    echo "TEST FAILED !";
    #save contents when test failed
    mv $VTEST_HOME/ERROR-* results;
    jar cf $ZIPFILE results;
    exit 1;
else
    echo "TEST OK";
    cp $VTEST_HOME/joram/src/jndi2/report.txt results/jndi2-report.txt;
    cp $VTEST_HOME/joram/src/joram/report.txt results/joram-report.txt;
    cp $VTEST_HOME/joram/src/jms/report.txt results/jms-report.txt;
    jar cf $ZIPFILE results;
    exit 0;
fi
