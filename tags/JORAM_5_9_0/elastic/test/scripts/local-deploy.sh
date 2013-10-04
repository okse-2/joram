#!/bin/bash

DST=/home/ubuntu
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

rm -rf $DST/joram
cp -rf joram $DST

echo "STARTING SERVERS.."
for i in {0..3}
do
	$DST/joram/bin/server.sh $i &
done

