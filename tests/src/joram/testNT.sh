cd ../../../joram/src
cp fr/dyade/aaa/util/NTransactionNEW.java fr/dyade/aaa/util/NTransaction.java
ant -f joram.xml clean ship
cd ../../tests/src/joram
echo "########################################" >> report.txt
echo "# New NTransaction" >> report.txt
echo "########################################" >> report.txt
ant perf.test6
echo "########################################" >> report.txt
echo "# New NTransaction" >> report.txt
echo "########################################" >> report.txt
ant perf.test6
cd ../../../joram/src
cp fr/dyade/aaa/util/NTransactionOLD.java fr/dyade/aaa/util/NTransaction.java
ant -f joram.xml clean ship
cd ../../tests/src/joram
echo "########################################" >> report.txt
echo "# Old NTransaction" >> report.txt
echo "########################################" >> report.txt
ant perf.test6
echo "########################################" >> report.txt
echo "# Old NTransaction" >> report.txt
echo "########################################" >> report.txt
ant perf.test6
