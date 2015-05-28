#!/bin/bash

SIZE=6
PIKE=60

PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"

echo "SETTING SUBS' IPS..."
TMP=/tmp/instances
ec2-describe-instances | grep running > /tmp/instances
for i in $(seq 0 $((SIZE - 1)))
do
        j=$(( i + 3))
        SERV[$i]=$(cat $TMP | sed -n $j'p' | cut -f17)
        PRIV[$i]=$(cat $TMP | sed -n $j'p' | cut -f18)
        echo ${SERV[$i]} ${PRIV[$i]}
done
rm $TMP

for i in $(seq 0 $((SIZE - 1)))
do
    ssh $KEY ubuntu@${SERV[$i]} "rm log; killall -9 java"
done
