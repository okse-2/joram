# joramConfig.sh

# This file is intended to be executed in a bash session 
# using the . or source command, so that environment variables 
# are set in the current session. Those variables are used by 
# the generic make files, and are also needed when launching
# JORAM and demos. They must be personalized before the gnu 
# make is called.


#
# directories definitions
#

# root directory holds the src, classes (if you have 
# downloaded JORAM sources) and lib directories.
# In a windows environment, the drive letter is given 
# using the //d notation, not the d: or //D notations.
# The value of this variable must be the output of the 
# pwd command executed in that directory. It is case 
# sensitive.

# <--------------------------------- TO BE UPDATED 
# UNIX OS:
#export ROOTDIR=/home/joram1_1_0
# WINDOWS OS:
# Please respect the syntax "//drive/..."
export ROOTDIR=//d/joram1_1_0

# where java finds the language classes.
# UNIX OS:
#export JDKHOME=/usr/local/jdk1.2.2
# WINDOWS OS:
# Please respect the syntax "drive:/..."
export JDKHOME=c:/jdk1.2.2

# ----------------------------------------------->


# directory for Java source files
export SRCDIR=${ROOTDIR}/src
# directory for built Java class files
export OBJDIR=${ROOTDIR}/classes
# directory for imported and built Java jar library files
export LIBDIR=${ROOTDIR}/lib
# directory for shipping results
export SHIP=${ROOTDIR}/ship

# make file which should be included in all Makefiles
# provides standard variables and rules definitions
export GENERAL_MK=${SRCDIR}/makefiles/general.mk


#
# Java definitions
#
export JAVA=${JDKHOME}/bin/java
export JAVAPATH=${JDKHOME}/jre/lib/rt.jar
# jikes users: uncomment next line, comment 
# the line after...
#export JAVAC="c:/jikes/jikes -nowarn"
export JAVAC="${JDKHOME}/bin/javac -nowarn"

#
# build specific definitions
# use XML deployment script format
export GDT_FMT=XML


#
# CLASSPATH setting
#
if [ ${OSTYPE} == "cygwin32" ] 
then
    export PATH_SEP=";"
    rm -f in_tmp
    echo ${ROOTDIR} > in_tmp
    echo ${ROOTDIR} |grep "^//\([a-z]\)/joram" >/dev/null
    if [ $? -ne 0 ]
    then
	export ROOT=`sed 's$^/joram$c:/joram$' in_tmp`
    else
	export ROOT=`sed 's$^//\([a-z]\)/joram$\1:/joram$' in_tmp`
    fi
    rm -f in_tmp
else
    export PATH_SEP=:
    export ROOT=${ROOTDIR}
fi

if [ -z "${ROOT}" ]
then
    echo "Problem while configuring your CLASSPATH"
    exit 1
fi

if echo ${CLASSPATH} | grep ${ROOT}/lib/a3agent.jar >/dev/null
then	:
else
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${JAVAPATH}"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}."
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/classes"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3util.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3agent.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3ip.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3mom.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3ns.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/a3jndi.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/joram.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/xerces.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/jta-spec1_0_1.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/jms.jar"
    export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOT}/lib/jndi.jar"
fi
