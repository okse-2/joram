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
# <--------------------------------- TO BE UPDATED 

# root directory holds the src, classes (if you have 
# downloaded JORAM sources) and lib directories.
# In a windows environment, the drive letter is given 
# using the //d notation, not the d: notation or //D.
# The value of this variable must be the output of the 
# pwd command executed in that directory. It is case 
# sensitive.

# UNIX OS:
#export ROOTDIR=/home/joram1_1_0
# WINDOWS OS:
export ROOTDIR=//e/joram1_1_0
export WINROOTDIR=e:/joram1_1_0
# where java finds the language classes.
# UNIX OS:
#export JDKHOME=/usr/local/jdk1.2.2
# WINDOWS OS:
export JDKHOME=c:/java/jdk1.2.2

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

# <--------------------------------- TO BE UPDATED 
export JAVAC="c:/java/jikes/jikes -nowarn"
#export JAVAC="${JDKHOME}/bin/javac -nowarn"
# ----------------------------------------------->

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
    export ROOT=${WINROOTDIR}
else
    export PATH_SEP=:
    export ROOT=${ROOTDIR}
fi

if [ -z "${ROOT}" ]
then
    echo "Pb with your ROOT"
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
