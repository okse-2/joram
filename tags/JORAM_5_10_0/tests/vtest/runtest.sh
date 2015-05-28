#!/bin/bash
###################################
# Vtest linux script for JORAM    #
# $1 = version, sous form url SVN #
###################################

############
## Variable setting
############
EXPECTED_ARGS=1

#ensure existence of environment variable VTEST_HOME
if [ -z "$VTEST_HOME" ]
then
    echo "variable VTEST_HOME doesn't exist"
    echo "please set variable VTEST_HOME to the Vtest \"runtest\" script path"
    export VTEST_HOME="/home/vsadt"
else
    echo "VTEST_HOME set to $VTEST_HOME"
fi

if [ $# -ne $EXPECTED_ARGS ]
then
    echo "runtest is expecting a svn url"
    echo "Usage: `basename $0` svn_url"
    exit 1;
fi

source $VTEST_HOME/.bashrc

#value should be synchronized whith vtest configuration (test.properties file)
JORAM_TEST_DIR="joram-test"

date=`date +%x`
date=`echo $date | sed -e s:/:.:g`
LOGFILE=$VTEST_HOME/"vtest-$date.log"
ZIPFILE=$VTEST_HOME/"result.zip"

echo "printing JDK version"
java -version >> $LOGFILE 2>&1 
echo "--"

############
## Test installation
############
svn co svn://svn.forge.objectweb.org/svnroot/joram/$1/joram $VTEST_HOME/joram-src >> $LOGFILE 2>&1
cd $VTEST_HOME/joram-src ;
echo "installing joram sources from url $1"
mvn install >> $LOGFILE 2>&1

cd $VTEST_HOME/$JORAM_TEST_DIR ;

#installing joram tests using maven
echo "installing joram tests"
mvn install >> $LOGFILE 2>&1

############
## Launching tests
############
cd src;
echo "on launching ant tests.jms.all"
ant custom.tests.vtest -Dship.dir=../../../joram-src/ship >> $LOGFILE 2>&1
ant vtest.check.reports -Dship.dir=../../../joram-src/ship >> $LOGFILE 2>&1 
TEST_RESULT=$?;

############
## Collecting results
############
mkdir results ;
cp $LOGFILE results ;

if [[ $TEST_RESULT -gt 0 ]]; then
    echo "TEST FAILED !";
    #save contents when test failed
    mv $VTEST_HOME/ERROR-* results;
    jar cf $ZIPFILE results;
    exit 1;
else
    echo "TEST OK";
    cp $VTEST_HOME/joram-test/src/jndi2/report.txt results/jndi2-report.txt;
    cp $VTEST_HOME/joram-test/src/joram/report.txt results/joram-report.txt;
    cp $VTEST_HOME/joram-test/src/jms/report.txt results/jms-report.txt;
    jar cf $ZIPFILE results;
    exit 0;
fi
