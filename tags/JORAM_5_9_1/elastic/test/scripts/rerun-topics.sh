#!/bin/bash

NB_VMS=5
NB_JOR=3

PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"


echo "SETTING VMS IPS..."
TMP=/tmp/instances
ec2-describe-instances | grep running > /tmp/instances
for i in $(seq 0 $((NB_VMS - 1)))
do
	j=$(($i + 1))
        SERV[$i]=$(cat $TMP | sed -n $j'p' | cut -f17)
        PRIV[$i]=$(cat $TMP | sed -n $j'p' | cut -f18)
	echo ${SERV[$i]} ${PRIV[$i]}
done
rm $TMP

echo "CLEANING..."
for i in $(seq 0 $((NB_VMS - 1)))
do
	ssh $KEY ubuntu@${SERV[$i]} killall -9 java
	ssh $KEY ubuntu@${SERV[$i]} rm -rf joram/run/*
done

echo "RUNNING SERVERS..."
for i in $(seq 0 $((NB_JOR - 1)))
do
	ssh $KEY ubuntu@${SERV[$i]} "nohup joram/bin/server.sh $i  > /dev/null &"
done

echo "ADMINISRATING..."
ssh $KEY ubuntu@${SERV[0]} "nohup joram/bin/client.sh elasticity.topics.Setup $1 &"
