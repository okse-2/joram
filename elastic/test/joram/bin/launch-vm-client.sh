#!/bin/bash

IPADDR=$1
NUM=$2

ssh 	-o UserKnownHostsFile=/dev/null \
        -o StrictHostKeyChecking=no \
        -i /home/ubuntu/joram/aws/joram.pem \
        -f $IPADDR "nohup /home/ubuntu/joram/bin/client.sh elasticity.eval.Worker $NUM > worker$2.log &"
