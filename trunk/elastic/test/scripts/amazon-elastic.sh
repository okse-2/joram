SERVP1=$(ec2-describe-instances | grep running | sed -n '1p' | cut -f17)
SERVP2=$(ec2-describe-instances | grep running | sed -n '2p' | cut -f17)
SERVW1=$(ec2-describe-instances | grep running | sed -n '3p' | cut -f17)
TMPLTE=$(ec2-describe-instances | grep running | sed -n '4p' | cut -f17)

PEM="/home/elrhedda/Amazon/joram.pem"
KEY="-i $PEM"

echo "BUILDING.."
echo "JORAM"
cd ../../../joram
#mvn install
if [ $? -ne 0 ]
then
	echo "BUILD ERROR!"
	exit 1
fi

echo "ELASTIC"
cd ../elastic/java/src
ant clean compile
if [ $? -ne 0 ]
then
        echo "BUILD ERROR!"
	exit 1
fi

echo "SETTING CONFIG FILE"
PRIV101=$(ec2-describe-instances | grep running | sed -n '1p' | cut -f18)
PRIV102=$(ec2-describe-instances | grep running | sed -n '2p' | cut -f18)
PRIV001=$(ec2-describe-instances | grep running | sed -n '3p' | cut -f18)
cd ../../test/joram/config
sed	"s/JNDIHOST/$PRIV101/" \
	jndi.properties.template > jndi.properties
sed 	-e "s/HOST101/$PRIV101/" \
	-e "s/HOST102/$PRIV102/" \
	-e "s/HOST001/$PRIV001/" \
	a3servers.xml.template > a3servers.xml

#cat a3servers.xml

echo "LOCAL COPYING.."
cd ../..
rm -rf joram/classes joram/ship
cp -rf ../java/classes joram
cp -rf ../../joram/ship joram
cp bundles/* joram/ship/bundle
cp -rf ../java/aws joram/ship


echo "DEPLOYING..."
ssh $KEY ubuntu@$SERVP1 killall -9 java
ssh $KEY ubuntu@$SERVP1 rm -rf joram *.log
scp $KEY -r joram ubuntu@$SERVP1: > /dev/null

ssh $KEY ubuntu@$SERVP2 killall -9 java
ssh $KEY ubuntu@$SERVP2 rm -rf joram *.log
scp $KEY -r joram ubuntu@$SERVP2: > /dev/null

ssh $KEY ubuntu@$SERVW1 killall -9 java
ssh $KEY ubuntu@$SERVW1 rm -rf joram *.log
scp $KEY -r joram ubuntu@$SERVW1: > /dev/null

ssh $KEY ubuntu@$TMPLTE rm -rf joram
scp $KEY -r joram ubuntu@$TMPLTE: > /dev/null

echo "PUBLISHING IMAGE..."
OLDIMAGE=$(ec2-describe-images | grep "JoramVM" | cut -f2)
ec2-deregister $OLDIMAGE
echo "Removed $OLDIMAGE!"

INSTANCE=$(ec2-describe-instances | grep running | sed -n '4p' | cut -f2)
NEWIMAGE=$(ec2-create-image $INSTANCE -n "JoramVM" | cut -f2)
echo "Created $NEWIMAGE!"

ssh $KEY ubuntu@$SERVP1 sed -i "s/IMAGE_ID/$NEWIMAGE/g" joram/aws/amazon.properties

sleep 60

echo "RUNNING SERVERS..."
ssh $KEY ubuntu@$SERVP1 "nohup joram/bin/server.sh 101 > /dev/null &"
ssh $KEY ubuntu@$SERVP2 "nohup joram/bin/server.sh 102 > /dev/null &"
ssh $KEY ubuntu@$SERVW1 "nohup joram/bin/server.sh 1 > /dev/null &"
ssh $KEY ubuntu@$SERVW1 "nohup joram/bin/server.sh 2 > /dev/null &"

echo "ADMINISTRATING.."
ssh $KEY ubuntu@$SERVP1 "nohup joram/bin/client.sh elasticity.eval.Setup &"

echo "LAUNCHING CLIENTS.."
ssh $KEY ubuntu@$SERVW1 "nohup joram/bin/client.sh elasticity.eval.Worker 1 > worker1.log &"

ssh $KEY ubuntu@$SERVP1 "nohup joram/bin/client.sh elasticity.eval.Producer 1 > producer1.log &"
ssh $KEY ubuntu@$SERVP2 "nohup joram/bin/client.sh elasticity.eval.Producer 2 > producer2.log &"

ssh $KEY ubuntu@$SERVP1 "nohup joram/bin/client.sh elasticity.loop.ControlLoop > elasticity.log &"

