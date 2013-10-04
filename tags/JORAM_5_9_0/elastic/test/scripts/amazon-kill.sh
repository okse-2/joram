#!/bin/bash

for VM in `ec2-describe-instances | grep INSTANCE | cut -f17 | head -n 3`
do
	ssh 	-i /home/elrhedda/Amazon/joram.pem \
		ubuntu@$VM killall java
done
