#!/bin/bash

LOGDIR=suite4/$1

if [ -d $LOGDIR ];
then
	echo "ERROR: log directory exists already."
	exit
else
	mkdir $LOGDIR
fi
for i in {0..3}
do
	scp vm$i:*.log $LOGDIR/
	scp vm$i:joram/run/server*/*.csv $LOGDIR
done
