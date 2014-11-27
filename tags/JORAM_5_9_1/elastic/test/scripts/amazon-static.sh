#!/bin/bash

KEY="-i /home/elrhedda/Amazon/joram.pem"
JRE=$JAVA_HOME
VMS=()

for ip in `ec2-describe-instances | grep INSTANCE | cut -f17`
do
	VMS+=($ip)
done 

if [ "$1" = "stop" ]
then
	echo "KILLALL.."
	for ip in ${VMS[*]}
	do
        	VM=ubuntu@$ip
        	ssh $KEY $VM killall -9 java
		echo "Done for $ip.."
	done
	exit 0;
fi

echo "BUILDING.."
echo "JORAM"
cd ../../../joram
mvn install | grep BUILD

echo "ELASTIC"
cd ../elastic/java/src
ant clean compile | grep BUILD

echo "LOCAL COPYING.."
cd ../../test
rm -rf joram/classes joram/ship
cp -rf ../java/classes joram
cp -rf ../../joram/ship joram
cp bundles/* joram/ship/bundle
cp -rf ../java/aws joram/ship

echo "DEPLOYING..."
LIM=$(( ${#VMS[@]} - 1 ))

for i in $(seq 0 $LIM)
do
	VM=ubuntu@${VMS[$i]}
	ssh $KEY $VM killall -9 java
	ssh $KEY $VM rm -rf joram *.log
	scp $KEY -r joram $VM:
	if [ "$1" = "java" ]
	then
		scp $KEY -r $JRE $VM:
	fi
done

echo "RUNNING SERVERS..."
for i in $(seq 0 $LIM)
do
	VM=ubuntu@${VMS[$i]}
	ssh $KEY $VM "nohup joram/bin/server.sh $i > server$i.log &"
done

echo "ADMINISTRATING.."
ssh $KEY ubuntu@${VMS[0]} "nohup joram/bin/client.sh elasticity.old.StaticSetup &"

echo "LAUNCHING CLIENTS.."
ssh $KEY ubuntu@${VMS[0]} "nohup joram/bin/client.sh elasticity.old.RegulatedSender 0 > rsender0.log &"
ssh $KEY ubuntu@${VMS[3]} "nohup joram/bin/client.sh elasticity.old.RegulatedSender 3 > rsender3.log &"
ssh $KEY ubuntu@${VMS[1]} "nohup joram/bin/client.sh elasticity.old.RegulatedReceiver 1 > rreceiver1.log &"
ssh $KEY ubuntu@${VMS[2]} "nohup joram/bin/client.sh elasticity.old.RegulatedReceiver 2 > rreceiver2.log &"

