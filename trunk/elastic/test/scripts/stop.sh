#/bin/bash

for i in {2..4}
do
	ssh 10.0.0.$i killall -9 java
done
