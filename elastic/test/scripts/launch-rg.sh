#!/bin/bash

#if [ $# -ne 2 ];
#then
#	echo "ERROR: needs 2 parameters, received $#."
#	exit
#fi
#
#if [ $[$1+$2] -ne 100 ];
#then
#	echo "ERROR: arguments should sum up to 100."
#	exit
#fi

echo "DEPLOYING.."
./deploy.sh


echo "LAUNCHING CLIENTS"
ssh vm0 "nohup joram/bin/client.sh alias.RegulatedSender > sender.log &"
ssh vm3 "nohup joram/bin/client.sh alias.RegulatedSender 3 > sender3.log &"
ssh vm1 "nohup joram/bin/client.sh alias.RegulatedReceiver 1 $1 > receiver1.log &"
ssh vm2 "nohup joram/bin/client.sh alias.RegulatedReceiver 2 $2 > receiver2.log &"

