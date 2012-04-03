#!/bin/bash

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

echo "DEPLOYING VMS.."
for i in {0..2}
do
	ssh vm$i killall -9 java
	ssh vm$i rm -rf joram *.log
	scp -r joram vm$i:. > /dev/null
done

echo "RUNNING SERVERS.."
ssh vm0 "nohup joram/bin/server.sh 0 > /dev/null &"
ssh vm1 "nohup joram/bin/server.sh 1 > /dev/null &"
ssh vm1 "nohup joram/bin/server.sh 2 > /dev/null &"
ssh vm2 "nohup joram/bin/server.sh 3 > /dev/null &"
ssh vm2 "nohup joram/bin/server.sh 4 > /dev/null &"

echo "ADMINISTRATING.."
ssh vm0 "nohup joram/bin/client.sh alias.Admin &"

echo "LAUNCHING CLIENTS.."
ssh vm1 "nohup joram/bin/client.sh alias.RegulatedReceiver 1 > receiver1.log &"
ssh vm1 "nohup joram/bin/client.sh alias.RegulatedReceiver 2 > receiver2.log &"
ssh vm2 "nohup joram/bin/client.sh alias.RegulatedReceiver 3 > receiver3.log &"
ssh vm2 "nohup joram/bin/client.sh alias.RegulatedReceiver 4 > receiver4.log &"
ssh vm0 "nohup joram/bin/client.sh alias.ElasticityLoop > elasticity.log &"
ssh vm0 "nohup joram/bin/client.sh alias.RegulatedSender > sender.log &"
echo "DONE."
