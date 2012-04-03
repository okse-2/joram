#!/bin/bash

for i in {0..2}
do
	ssh vm$i killall -9 java
done
