#!/bin/bash

NB_VMS=10
NB_JOR=4

LOG="/home/elrhedda/Joram/elastic/test/logs/last"
PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"

function set_ips {
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
}

function build_and_deploy {
  echo "BUILDING.."
  echo "JORAM.."
  cd ../../../joram
  #mvn install
  if [ $? -ne 0 ]
  then
    echo "BUILD ERROR!"
    exit 1
  fi

  echo "ELASTIC.."
  cd ../elastic/java/src
  ant clean compile
  if [ $? -ne 0 ]
  then
    echo "BUILD ERROR!"
    exit 1
  fi

  echo "SETTING CONFIG FILES"
  cd ../../test/joram/config

  sed "s/JNDIHOST/${PRIV[0]}/" \
    jndi.properties.template > jndi.properties

  cp a3servers.xml.t.template a3servers.xml
  for i in $(seq 0 $((NB_JOR - 1)))
  do
    sed -e "s/HOST$i/${PRIV[$i]}/" \
        -i a3servers.xml
  done

  echo "LOCAL COPYING.."
  cd ../..
  rm -rf joram/classes joram/ship
  cp -rf ../java/classes joram
  cp -rf ../../joram/ship joram
  cp bundles/* joram/ship/bundle
  cp -rf ../java/aws joram/ship

  echo "DEPLOYING..."
  for i in $(seq 0 $((NB_VMS - 1)))
  do
    ssh $KEY ubuntu@${SERV[$i]} killall -9 java
    ssh $KEY ubuntu@${SERV[$i]} rm -rf joram *.log
    scp $KEY -r joram ubuntu@${SERV[$i]}: > /dev/null
  done
}


function reinit {
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

  echo "ADMINISTRATING..."
    ssh $KEY ubuntu@${SERV[0]} "joram/bin/client.sh elasticity.topics.eval.Setup $1"
}

function get_logs {
  scp $KEY ubuntu@${SERV[$((NB_VMS - 1))]}:*.log $LOG
}

function run_round {
  NB_SUB=$((NB_VMS - NB_JOR))
  PART=$(($1 / NB_SUB))

  reinit $2

  for S in $(seq 0 $((NB_SUB - 1)))
  do
    I=$((S + NB_JOR))
    if [ "$2" -eq "0" ]
    then
      T=0
    else
      T=$(((S * $2) / NB_SUB + 1))
    fi

    ssh $KEY ubuntu@${SERV[$I]} "nohup joram/bin/client.sh elasticity.topics.eval.Sub $T $PART > $1-$2-$T-$S.log &"
    sleep 10
  done

  ssh $KEY ubuntu@${SERV[$((NB_VMS - 1))]} "nohup joram/bin/client.sh elasticity.topics.eval.Pub 0 10 1000 > /dev/null &"

  for i in $(seq 10)
  do
     echo "WAITING FOR RESULTS OF $1 $2 ($i)..."
     sleep 2
  done
  get_logs
}

set_ips
#build_and_deploy

echo "RUNNING TESTS"
for subs in $(seq 20 20 20)
do
  for topics in $(seq 0 3)
  do
    run_round $subs $topics
  done
done
