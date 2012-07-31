#/bin/bash

LOGDIR=suite8/$1

TMPDIR=$RANDOM
echo $TMPDIR

ssh molecule logs/getlog.sh $TMPDIR
scp -r molecule:logs/$TMPDIR/* $LOGDIR
