# joramConfig.sh

# This file is intended to be executed in a bash session 
# using the . or source command, so that environment variables 
# are set in the current session. Those variables are used by 
# the generic make files, and are also needed when launching
# JORAM and demos. They must be personalized before the gnu 
# make is called.


#
# OS definition
#
# <--------------------------------- TO BE UPDATED 

# UNIX OS:
export PATH_SEP=:
# WINDOWS OS:
#export PATH_SEP=;

# ----------------------------------------------->


#
# directories definitions
#
# <--------------------------------- TO BE UPDATED 

# root directory holds the src, classes (if you have 
# downloaded JORAM sources) and lib directories.
# In a windows environment, the drive letter is given 
# using the //d notation, not the d: notation.
# The value of this variable must be the output of the 
# pwd command executed in that directory. It is case 
# sensitive.

export ROOTDIR=/home/joram1_1_0

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
# <--------------------------------- TO BE UPDATED 
# where java finds the language classes.

export JDKHOME=/usr/local/jdk1.2.2
export JAVA=${JDKHOME}/bin/java
export JAVAPATH=${JDKHOME}/jre/lib/rt.jar
export JAVAC=jikes
# export JAVAC="${JDKHOME}/bin/javac \
#     -bootclasspath \"${ROOTDIR}/lib/OB.jar;${JAVAPATH}\""

# ----------------------------------------------->


#
# build specific definitions
# use XML deployment script format
export GDT_FMT=XML


#
# CLASSPATH setting
#
export CLASSPATH="${CLASSPATH}${PATH_SEP}${JAVAPATH}"
export CLASSPATH="${CLASSPATH}${PATH_SEP}."
export CLASSPATH="${CLASSPATH}${PATH_SEP}${ROOTDIR}"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${OBJDIR}"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3util.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3agent.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3ip.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3mom.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3ns.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/a3jndi.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/joram.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/xerces.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/jta-spec1_0_1.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/jms.jar"
export CLASSPATH="${CLASSPATH}${PATH_SEP}${LIBDIR}/jndi.jar"
