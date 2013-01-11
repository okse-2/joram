VMS=(46.51.145.182 46.137.69.24 176.34.205.183)
KEY="-i /home/elrhedda/Amazon/joram.pem"
JRE=$JAVA_HOME

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
for i in $(seq 1 $LIM)
do
        VM=ubuntu@${VMS[$i]}
	ssh $KEY $VM "nohup joram/bin/client.sh elasticity.old.RegulatedReceiver $i > rreceiver$i.log &"
done

ssh $KEY ubuntu@${VMS[0]} "nohup joram/bin/client.sh elasticity.old.RegulatedSender 0 > rsender.log &"
