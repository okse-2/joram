#!/bin/bash

for VM in `ec2-describe-instances | grep INSTANCE | cut -f17`
do
	ssh 	-i /home/elrhedda/Amazon/joram.pem \
		ubuntu@$VM killall java
done
