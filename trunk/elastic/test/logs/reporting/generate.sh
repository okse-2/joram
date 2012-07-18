#!/bin/bash

# Logs should be in sub-directories of LOGDIR!
LOGDIR=$1

mkdir loads
mkdir rates

./processLog.py $LOGDIR/*/*.csv

cd rates
gnuplot rates.gnuplot

cd ../loads
gnuplot loads.gnuplot

cd ..

epstopdf rates.eps
epstopdf loads.eps

pdflatex report.tex

rm -rf report.aux report.log loads* rates*

evince report.pdf
