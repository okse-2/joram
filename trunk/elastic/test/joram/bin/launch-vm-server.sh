#!/bin/bash

IPADDR=$1
NUM=$2

OK=1
while [ $OK -ne 0 ]
do
	scp -o UserKnownHostsFile=/dev/null \
	    -o StrictHostKeyChecking=no \
	    -i /root/config/molkey.pem \
            /root/joram/run/new_a3servers.xml \
	    $IPADDR:/root/joram/config/a3servers.xml
	OK=$?
	sleep 1
done
	
ssh -o UserKnownHostsFile=/dev/null \
    -o StrictHostKeyChecking=no \
    -i /root/config/molkey.pem \
    -f $IPADDR "nohup joram/bin/server.sh $NUM > /dev/null &"
