SERVT0=$(ec2-describe-instances | grep running | sed -n '1p' | cut -f17)
SERVT1=$(ec2-describe-instances | grep running | sed -n '2p' | cut -f17)
SERVTM=$(ec2-describe-instances | grep running | sed -n '3p' | cut -f17)

PEM="/home/elrhedda/Joram/amazon/joram.pem"
KEY="-i $PEM"

echo "BUILDING.."
echo "JORAM"
cd ../../../joram
mvn install
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
PRIVT0=$(ec2-describe-instances | grep running | sed -n '1p' | cut -f18)
PRIVT1=$(ec2-describe-instances | grep running | sed -n '2p' | cut -f18)
cd ../../test/joram/config
sed	"s/JNDIHOST/$PRIVT0/" \
	jndi.properties.template > jndi.properties
sed 	-e "s/HOST0/$PRIVT0/" \
	-e "s/HOST1/$PRIVT1/" \
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
ssh $KEY ubuntu@$SERVT0 killall -9 java
ssh $KEY ubuntu@$SERVT0 rm -rf joram *.log
scp $KEY -r joram ubuntu@$SERVT0: > /dev/null

ssh $KEY ubuntu@$SERVT1 killall -9 java
ssh $KEY ubuntu@$SERVT1 rm -rf joram *.log
scp $KEY -r joram ubuntu@$SERVT1: > /dev/null

ssh $KEY ubuntu@$SERVT1 killall -9 java
ssh $KEY ubuntu@$SERVTM rm -rf joram
scp $KEY -r joram ubuntu@$SERVTM: > /dev/null

echo "PUBLISHING IMAGE..."
OLDIMAGE=$(ec2-describe-images | grep "JoramVM" | cut -f2)
ec2-deregister $OLDIMAGE
echo "Removed $OLDIMAGE!"

INSTANCE=$(ec2-describe-instances | grep running | sed -n '3p' | cut -f2)
NEWIMAGE=$(ec2-create-image $INSTANCE -n "JoramVM" | cut -f2)
echo "Created $NEWIMAGE!"

ssh $KEY ubuntu@$SERVT0 sed -i "s/IMAGE_ID/$NEWIMAGE/g" joram/aws/amazon.properties

sleep 60

echo "RUNNING SERVERS..."
ssh $KEY ubuntu@$SERVT0 "nohup joram/bin/server.sh 0 > /dev/null &"
ssh $KEY ubuntu@$SERVT1 "nohup joram/bin/server.sh 1 > /dev/null &"

echo "ADMINISTRATING.."
#ssh $KEY ubuntu@$SERVT0 "nohup joram/bin/client.sh elasticity.eval.Setup &"

echo "LAUNCHING CLIENTS.."
#ssh $KEY ubuntu@$SERVT0 "nohup joram/bin/client.sh elasticity.loop.ControlLoop > elasticity.log &"
