#!/bin/bash
#################################
# Vtest linux script for JORAM  #
#################################
EXPECTED_ARGS=1

#ensure existence of environment variable VTEST_HOME
if [ -z "$VTEST_HOME" ]
then
    echo "variable VTEST_HOME doesn't exist"
    echo "please set variable VTEST_HOME to the Vtest \"runtest\" script path"
    export VTEST_HOME="/home/vsadt"
    #exit 1
else
    echo "VTEST_HOME set to $VTEST_HOME"
fi

if [ $# -ne $EXPECTED_ARGS ]
then
    echo "runtest is expecting a svn url"
    echo "Usage: `basename $0` svn_url"
    exit 1;
fi

echo "trying to setenv java 7"
/bin/bash $VTEST_HOME/setenv.sh 7 64;
if [[ $? -gt 0 ]]; then
    echo "SETENV FAILED !";
    exit 1;
else
    echo "SETENV OK";
fi
java -version

source $VTEST_HOME/.bashrc

#change to "tags/JORAM_X_Y_Z" when producing a release

#value should be synchronized whith vtest configuration (test.properties file)
JORAM_TEST_DIR="joram-test"

date=`date +%x`
date=`echo $date | sed -e s:/:.:g`
LOGFILE=$VTEST_HOME/"vtest-$date.log"
ZIPFILE=$VTEST_HOME/"result.zip"

#extracting joram source so as to test updated trunk artifacts
svn co svn://svn.forge.objectweb.org/svnroot/joram/$1/joram $VTEST_HOME/joram-src >> $LOGFILE 2>&1
cd $VTEST_HOME/joram-src ;
echo "installing joram trunk"
mvn install >> $LOGFILE 2>&1

#following command suppose that svn check out has been made into path $VTEST_HOME/joram
cd $VTEST_HOME/$JORAM_TEST_DIR ;

#installing joram tests using maven
echo "installing joram tests"
mvn install >> $LOGFILE 2>&1

#launching tests
cd src;
echo "on launching ant tests.jms.all"
ant tests.jms.all -Dship.dir=../../../joram-src/ship >> $LOGFILE 2>&1 
TEST_RESULT=$?;

mkdir results ;
cp $LOGFILE results ;

#getting test exit code
if [[ $TEST_RESULT -gt 0 ]]; then
    echo "TEST FAILED !";
    #save contents when test failed
    mv $VTEST_HOME/ERROR-* results;
    jar cf $ZIPFILE results;
    exit 1;
else
    echo "TEST OK";
    jar cf $ZIPFILE results;
    exit 0;
fi
