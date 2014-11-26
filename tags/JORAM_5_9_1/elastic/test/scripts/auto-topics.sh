#!/bin/bash

NB_VMS=5
NB_JOR=3

LOG="/home/elrhedda/Lab/joram/logs/13-10-11"
PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"

function reinit_config {
  echo "CLEANING..."
  for i in $(seq 0 $((NB_VMS - 1)))
  do
    ssh $KEY ubuntu@${SERV[$i]} killall -9 java
    ssh $KEY ubuntu@${SERV[$i]} rm -rf joram/run/* *.log
  done

  echo "RUNNING SERVERS..."
  for i in $(seq 0 $((NB_JOR - 1)))
  do
    ssh $KEY ubuntu@${SERV[$i]} "nohup joram/bin/server.sh $i  > /dev/null &"
  done

  echo "ADMINISRATING..."
    ssh $KEY ubuntu@${SERV[0]} "nohup joram/bin/client.sh elasticity.topics.Setup $1 &"
}

function get_logs {
  scp $KEY ubuntu@${SERV[3]}:*.log $LOG
  scp $KEY ubuntu@${SERV[4]}:*.log $LOG
}

function run_round {
  HALF=$(($1 / 2))
  if [ "$2" == "scalable" ]
  then
    T1=1
    T2=2
    CS="s"
  else
    T1=0
    T2=0
    CS="c"
  fi

  reinit_config $2

  echo "EXECUTING..."
  ssh $KEY ubuntu@${SERV[3]} "nohup joram/bin/client.sh elasticity.topics.Sub $T1 $HALF > $1-$CS-1.log &"
  echo "WAITING FOR SUBSCRIBERS ON 1.."
  sleep 10 # Wait for subscriptions
  ssh $KEY ubuntu@${SERV[4]} "nohup joram/bin/client.sh elasticity.topics.Sub $T2 $HALF > $1-$CS-2.log &"
  echo "WAITING FOR SUBSCRIBERS ON 2.."
  sleep 10
  ssh $KEY ubuntu@${SERV[4]} "nohup joram/bin/client.sh elasticity.topics.Pub 0 100 2000 > /dev/null &"

  for i in $(seq 100)
  do
     echo "WAITING FOR RESULTS OF $1 $2, $i..."
     sleep 3 
  done
  get_logs
}

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

echo "RUNNING TESTS:"
for j in $(seq 10 10 90)
do
  run_round $j
  run_round $j scalable
done
