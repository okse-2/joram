#/bin/bash

LOGDIR=suite7/$1

if [ -d $LOGDIR ];
then
	echo "ERROR: log directory exists already."
	exit
else
	mkdir $LOGDIR
fi

scp 10.0.0.2:joram/run/alias.ElasticityLoop.log $LOGDIR

for i in {2..20}
do
	scp 10.0.0.$i:joram/run/server*/*.csv $LOGDIR
done
