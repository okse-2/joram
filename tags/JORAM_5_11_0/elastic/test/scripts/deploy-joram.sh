#!/bin/bash

NB_VMS=8
NB_JOR=2


PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"

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

echo "SETTING CONFIG FILES"
cd ../../test/joram/config

sed	"s/JNDIHOST/${PRIV[0]}/" \
	jndi.properties.template > jndi.properties

cp a3servers.xml.t.template a3servers.xml
for i in $(seq 0 $((NB_JOR - 1)))
do
	sed 	-e "s/HOST$i/${PRIV[$i]}/" \
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

echo "RUNNING SERVERS..."
for i in $(seq 0 $((NB_JOR - 1)))
do
	ssh $KEY ubuntu@${SERV[$i]} "nohup joram/bin/server.sh $i  > /dev/null &"
done
