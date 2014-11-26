#!/bin/bash
#################################
# Vtest linux script for JORAM  #
#################################

#ensure existence of environment variable VTEST_HOME
# if [ -z "$VTEST_HOME" ]
# then
#     echo "variable VTEST_HOME doesn't exist"
#     echo "please set variable VTEST_HOME to the Vtest \"runtest\" script path"
#     else
#     echo "VTEST_HOME set to $VTEST_HOME"
# fi

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
    echo "runtest is expecting two args"
    echo "Usage: `basename $0` svn_url jdk_number"
    exit 1;
fi

if [ $1 -eq "6" ]; then
	echo "setting jdk 6 for cygwin"
	export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.6.0_35"
	export JAVA="$JAVA_HOME/bin"
	export PATH=$JAVA:$PATH
elif [ $1 -eq "7" ]; then
	echo "setting jdk 7 for cygwin"
	export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.7.0_09"
	export JAVA="$JAVA_HOME/bin"
	export PATH=$JAVA:$PATH
else
	echo "jdk $2 not supported"
fi

VTEST_HOME="/cygdrive/c/vtest"
date=`date +%x`
date=`echo $date | sed -e s:/:.:g`
LOGFILE=$VTEST_HOME/"vtest-$date.log"
ZIPFILE="result.zip"

#extracting joram source so as to test updated trunk artifacts
svn co svn://svn.forge.objectweb.org/svnroot/joram/$2/joram $VTEST_HOME/joram-src >> $LOGFILE 2>&1
cd $VTEST_HOME/joram-src ;
echo "installing joram trunk"
mvn install >> $LOGFILE 2>&1

#following command suppose that svn check out has been made into path $VTEST_HOME/joram
cd $VTEST_HOME/joram-test ;

#installing joram tests using maven
echo "installing joram tests"
mvn install >> $LOGFILE 2>&1

#launching tests
cd $VTEST_HOME;
echo "on launching ant custom.tests.vtest"

cmd <<EOF
runtest.bat $1 >> antrun.txt
EOF

#ant vtest.check.reports >> $LOGFILE 2>&1 
TEST_RESULT=$?;

mkdir results ;
cp $LOGFILE results ;
cp antrun.txt results;

#getting test exit code
if [[ $TEST_RESULT -gt 0 ]]; then
    echo "TEST FAILED !";
    #save contents when test failed
    mv $VTEST_HOME/ERROR-* results;
    jar cf $ZIPFILE results;
    mv $ZIPFILE ~
    exit 1;
else
    echo "TEST OK";
    # uncomment when using real joram test
    # cp $VTEST_HOME/joram-test/src/jndi2/report.txt results/jndi2-report.txt;
    # cp $VTEST_HOME/joram-test/src/joram/report.txt results/joram-report.txt;
    # cp $VTEST_HOME/joram-test/src/jms/report.txt results/jms-report.txt;
    jar cf $ZIPFILE results;
    mv $ZIPFILE ~
    exit 0;
fi
