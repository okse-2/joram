# @(#) $Id: build.sh,v 1.1 2001-08-31 08:14:09 tachkeni Exp $

DFLT_SHIP_FILE=$SRCDIR/makefiles/ship/scalagent.conf
SHIPDIR=$SHIP

if [ -z $1 ]
then
    SHIP_FILE=$DFLT_SHIP_FILE
else
    SHIP_FILE=$1
fi

if [ ! -f $SHIP_FILE ]
then
   echo "Missing file $SHIP_FILE."
   exit 1
fi

jar_name=`basename $SHIP_FILE | sed 's:\.conf:\.jar:g'`

if [ -z $ROOTDIR ]
then
    echo "you must set ROOTDIR"
    exit 1
fi
if [ -z $SHIPDIR ]
then
    echo "you must set SHIPDIR"
    exit 1
fi
if [ -z $OBJDIR ]
then
    echo "you must set OBJDIR"
    exit 1
fi

rm_ship=`echo $* | awk '/rm_ship/{print 1}'`
if [ ! -z $rm_ship ]
then
    echo
    echo "rm -fr $SHIPDIR"
    rm -fr $SHIPDIR
    echo "mkdir -p $SHIPDIR"
    mkdir -p $SHIPDIR
fi

echo
#echo "cd $ROOTDIR"
echo
cd $ROOTDIR
base_classe=`basename $OBJDIR`

cat $SHIP_FILE | while read line
do
    src=`echo $line | awk '!/^#/{print $1}'`
    if [ -z $src ]
    then
	continue
    fi

    dst=`echo $line | awk '{print $2}'`
    chmod=`echo $line | awk '{print $3}'`

    if [ $dst = "classes/"  ]
    then
        echo "cp -fP $base_classe/$src $SHIPDIR"
        cp -fP $base_classe/$src $SHIPDIR
        if [ 1 -eq $? ]
        then
            exit 1
        fi
    continue
    fi

    src=$ROOTDIR/$src
    dst=$SHIPDIR/$dst

    if [ ! -d $dst ]
    then
	echo "mkdir $dst"
	mkdir -p $dst
	if [ 1 -eq $? ]
	then
	    exit 1
	fi
    fi

    echo "cp -fR $src $dst"
    cp -fR $src $dst
    if [ 1 -eq $? ]
    then
        exit 1
    fi

    if [ ! -z $chmod ]
    then
	for s in $src
	do
	    f=$dst`basename $s`
	    echo "chmod $chmod $f"
	    chmod $chmod $f
        done
    fi
done

if [ "cygwin32" = $OSTYPE ] 
then
    SHIPDIR=`echo $SHIPDIR | sed 's$^//\([a-z]\)/$\1:/$'`
fi

if [ -d $SHIPDIR/$base_classe ]
then
    cd $SHIPDIR/$base_classe
    echo "creation de $jar_name"
    echo "wait..."
    jar cf $jar_name *
    if [ ! -d  $SHIPDIR/lib ]
    then
        mkdir -p $SHIPDIR/lib
    fi
    cp $SHIPDIR/$base_classe/$jar_name $SHIPDIR/lib
    cd $SHIPDIR
    rm -fr $SHIPDIR/$base_classe
    echo "$jar_name DONE."
fi

exit 0