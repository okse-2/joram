#!/bin/bash

echo "DEPLOYING.."
./deploy.sh


echo "LAUNCHING CLIENTS"
ssh vm0 "nohup joram/bin/client.sh alias.RoundSender $1 > sender.log &"
ssh vm0 "nohup joram/bin/client.sh alias.RoundReceiver 1 > receiver1.log &"
#ssh vm2 "nohup joram/bin/client.sh alias.RoundReceiver 2 > receiver2.log &"

