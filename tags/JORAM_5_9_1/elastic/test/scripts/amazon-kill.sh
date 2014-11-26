#!/bin/bash

for VM in `ec2-describe-instances | grep INSTANCE | cut -f17`
do
	ssh 	-i /home/elrhedda/Joram/amazon/joram.pem \
		ubuntu@$VM killall java
done
