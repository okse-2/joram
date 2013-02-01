SERVP1=10.0.0.2
SERVP2=10.0.0.3
SERVW1=10.0.0.4
HOST=molecule

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
ssh $SERVP1 killall -9 java
ssh $SERVP1 rm -rf joram *.log
scp -r joram $SERVP1: > /dev/null

ssh $SERVP2 killall -9 java
ssh $SERVP2 rm -rf joram *.log
scp -r joram $SERVP2: > /dev/null

ssh $SERVW1 killall -9 java
ssh $SERVW1 rm -rf joram *.log
scp -r joram $SERVW1: > /dev/null


scp -r joram $HOST:joram-factory/pack > /dev/null

echo "PUBLISHING IMAGE..." 
scp scripts/pub-joram-image.sh $HOST:joram-factory > /dev/null
ssh $HOST joram-factory/pub-joram-image.sh

echo "RUNNING SERVERS..."
ssh $SERVP1 "nohup joram/bin/server.sh 101 > /dev/null &"
ssh $SERVP2 "nohup joram/bin/server.sh 102 > /dev/null &"
ssh $SERVW1 "nohup joram/bin/server.sh 1 > /dev/null &"

echo "ADMINISTRATING.."
ssh $SERVP1 "nohup joram/bin/client.sh elasticity.eval.Setup &"

echo "LAUNCHING CLIENTS.."
ssh $SERVW1 "nohup joram/bin/client.sh elasticity.eval.Worker 1 > worker1.log &"

ssh $SERVP1 "nohup joram/bin/client.sh elasticity.eval.Producer 1 > producer1.log &"
ssh $SERVP2 "nohup joram/bin/client.sh elasticity.eval.Producer 2 > producer2.log &"

ssh $SERVP1 "nohup joram/bin/client.sh elasticity.loop.ControlLoop > elasticity.log &"

