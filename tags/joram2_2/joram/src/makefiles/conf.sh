#
# This file is intended to be executed in a bash session using the . command,
# so that environment variables are set in the current session. Those variables
# are used by the generic make files. They must be personalized before the
# gnu make is called.
#

alias pushc="pushd \`pwd | sed 'sS/srcS/classesS'\`"

# alias java="/jdk1.3/bin/java"
# alias javac="/jdk1.3/bin/javac"
# alias jar="/jdk1.3/bin/jar"

#
# target system type definitions
#

# target system type, UNIX or WINDOWS
export TARGET_SYSTEM=UNIX
#export TARGET_SYSTEM=WINDOWS

# separator used in java path option
if [ -z "$PATH_SEP" ]
then
  if [ "$TARGET_SYSTEM" = "UNIX" ]
  then
    export PATH_SEP=':'
  elif [ "$TARGET_SYSTEM" = "WINDOWS" ]
  then
    export PATH_SEP=';'
  fi
fi


#
# directories definitions
#

# root directory which holds the src directory
# in a windows environment, the drive letter is given using the
# //d notation, not the d: notation.
# The value of this variable must be the output of the pwd command
# executed in that directory. It is case sensitive.
export ROOTDIR=~/dev/joram-2.2

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

#export PATH=/jdk1.3/bin:${PATH}

# where java finds the language classes
#export JDKHOME=C:/jdk1.3
#export JAVAPATH=${JDKHOME}/jre/lib/rt.jar
#export JAVAC=C:/jikes/bin/jikes
# export JAVAC="${JDKHOME}/bin/javac \
#     -bootclasspath \"${ROOTDIR}/lib/OB.jar;${JAVAPATH}\""


#
# build specific definitions
#

# use XML deployment script format
export GDT_FMT=XML
