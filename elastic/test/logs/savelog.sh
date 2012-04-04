#/bin/bash

LOGDIR=suite5/$1

if [ -d $LOGDIR ];
then
	echo "ERROR: log directory exists already."
	exit
else
	mkdir $LOGDIR
fi

scp vm0:joram/run/alias.ElasticityLoop.log $LOGDIR

for i in {0..2}
do
	scp vm$i:*.log $LOGDIR/
	scp vm$i:joram/run/server*/*.csv $LOGDIR
done
