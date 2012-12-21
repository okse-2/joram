#!/bin/bash

# Logs should be in sub-directories of LOGDIR!
LOGDIR=$1

mkdir loads
mkdir rates

./processLog.py $LOGDIR/*/*.csv

#cd rates
#gnuplot rates.gnuplot

#cd ../loads
#gnuplot loads.gnuplot

#cd ..

#epstopdf rates.eps
#epstopdf loads.eps

cd plus
gnuplot nbrw.gnuplot
gnuplot suml.gnuplot
gnuplot maxl.gnuplot
cd ..

epstopdf plus/nbrw.eps -o=nbrw.pdf
epstopdf plus/suml.eps -o=suml.pdf
epstopdf plus/maxl.eps -o=maxl.pdf

pdflatex report.tex

rm -rf report.aux report.log loads* rates*
rm -rf plus/maxl.dat plus/suml.dat plus/nbrw.dat maxl.pdf suml.pdf nbrw.pdf

evince report.pdf &
