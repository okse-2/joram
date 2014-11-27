#/bin/bash

KEY=/home/elrhedda/Amazon/joram.pem
LOGS=.
DIR=$LOGS/$1

mkdir $DIR
if [ $? -ne 0 ]
then
	exit 0
fi

for ip in `ec2-describe-instances | grep INSTANCE | cut -f17`
do
	VM=ubuntu@$ip
	scp -i $KEY $VM:*.log $VM:joram/run/server*/*.csv $DIR
done

echo "Done."
