#!/bin/bash

if [ "$1" = "all" ]
then
	START=1
else
	START=4
fi

for VM in $(ec2-describe-instances | grep running | tail -n +$START | cut -f2)
do
	ec2-terminate-instances $VM
done
