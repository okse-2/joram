#!/bin/bash

IPADDR=$1
NUM=$2

ssh 	-o UserKnownHostsFile=/dev/null \
        -o StrictHostKeyChecking=no \
        -i /root/config/molkey.pem \
        -f $IPADDR "nohup joram/bin/client.sh elasticity.eval.Worker $NUM > /dev/null &"
