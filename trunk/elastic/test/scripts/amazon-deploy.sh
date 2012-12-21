VMS=(54.247.42.157 79.125.66.239 79.125.61.38)
KEY="-i /home/ahmed/AWS/amazon.pem"
JRE="/home/ahmed/Oracle/jdk1.7.0_02"

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
ssh $KEY ubuntu@${VMS[1]} "nohup joram/bin/client.sh elasticity.old.StaticSetup &"

#echo "LAUNCHING CLIENTS.."
#for i in {1..3}
#do
#        VM=10.0.0.$(($i+2))
#	ssh $VM "nohup joram/bin/client.sh elasticity.old.RegulatedReceiver $i > worker$i.log &"
#done

#ssh 10.0.0.2 "nohup joram/bin/client.sh elasticity.old.RegulatedSender 0 > producer.log &"
