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
for i in {0..3}
do
	VM=10.0.0.$(($i+2))
	ssh $VM killall -9 java
	ssh $VM rm -rf joram *.log
	scp -r joram $VM: > /dev/null
done

echo "RUNNING SERVERS..."
for i in {0..3}
do
	VM=10.0.0.$(($i+2))
	ssh $VM "nohup joram/bin/server.sh $i > server$i.log &"
done

echo "ADMINISTRATING.."
ssh 10.0.0.2 "nohup joram/bin/client.sh elasticity.old.StaticSetup &"

echo "LAUNCHING CLIENTS.."
for i in {0..3}
do
        VM=10.0.0.$(($i+2))
	ssh $VM "nohup joram/bin/client.sh elasticity.old.RegulatedReceiver $i > worker$i.log &"
done

ssh 10.0.0.2 "nohup joram/bin/client.sh elasticity.old.RegulatedSender 0 > producer.log &"

ssh 10.0.0.2 "nohup joram/bin/client.sh elasticity.old.ElasticityLoopV1 > elasticity.log &"
