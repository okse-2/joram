#!/bin/bash

IPADDR=$1
NUM=$2

OK=1
while [ $OK -ne 0 ]
do
	scp -o UserKnownHostsFile=/dev/null \
	    -o StrictHostKeyChecking=no \
	    -i /home/ubuntu/joram/aws/joram.pem \
            /home/ubuntu/joram/run/new_a3servers.xml \
	    ubuntu@$IPADDR:/home/ubuntu/joram/config/a3servers.xml
	OK=$?
	sleep 1
done

ssh -o UserKnownHostsFile=/dev/null \
    -o StrictHostKeyChecking=no \
    -i /home/ubuntu/joram/aws/joram.pem \
    -f ubuntu@$IPADDR "nohup /home/ubuntu/joram/bin/server.sh $NUM > /dev/null &"
