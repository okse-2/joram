#!/usr/bin/python

import sys
from datetime import datetime

OF = len("monitoring");     # Offset to get worker ID
TF = "%Y/%m/%d %H:%M:%S,%f" # Time format
SP = ";"                    # CSV files separator
MAX_RND = 500               # Maximum number of lines to read

# Gets the ID from a file name
def getId():
    start = fname.find("monitoring")+OF
    end = fname.find(".csv")
    return int(fname[start:end])

# Gets the the number of rounds since t0
def getRound():
    tc = datetime.strptime(cells[datei],TF)
    tr = tc - t0
    return int(round(tr.total_seconds() / 10))


# Main

# Init production
prod = [0] * MAX_RND

# Init time origin
datei = 0
for i in range(1,len(sys.argv)):
    fname = sys.argv[i]
    wId = getId()

    # Looking for worker 1
    if wId == 1:
        csv = open(fname,"r")
        csv.readline() # Skip first line.
        cells = csv.readline().split(SP)
        t0 = datetime.strptime(cells[datei],TF)
        csv.close()
        break

resId = 0
for i in range(1,len(sys.argv)):
    fname = sys.argv[i]
    if fname.find("10.0.0.2") > -1 or fname.find("10.0.0.3") > -1:
        continue # Skip producers

    wId = getId()

    csv = open(fname,"r")
    lines = csv.readlines()

    # Set relevant indexes
    recvi = 0
    loadi = 0
    cells = lines[1].split(SP)
    length = len(cells)
    for j in range(length):
        if cells[j].find("%d.%d.1026" % (wId,wId)) > -1:
            recvi = j+1
            loadi = j+3
            break

    # Process data
    resId += 1
    ratef = open("rates/rates%d.dat" % resId,"w")
    loadf = open("loads/loads%d.dat" % resId,"w")
    lrecv = 0
    for j in range(1,min(MAX_RND,len(lines))):
        cells = lines[j].split(SP)
        if len(cells) != length:
            break

        rnd = getRound()
        rate = (int(cells[recvi]) - lrecv) / 10
        lrecv = int(cells[recvi])
        load = int(cells[loadi])

        prod[rnd] += rate
        
        ratef.write("%d\t%d\n" % (rnd*10,rate))
        loadf.write("%d\t%d\n" % (rnd*10,load))

    csv.close()
    ratef.close()
    loadf.close()

# Create prod datafile.
prodf = open("rates/prod.dat","w")
for i in range(len(prod)):
    prodf.write("%d\t%d\n" % (i*10,prod[i]))

prodf.close()
    
# Generate Gnuplot scripts
ratep = open("rates/rates.gnuplot","w");
loadp = open("loads/loads.gnuplot","w");
headr = """# Gnuplot script
set terminal postscript eps enhanced
set output "../%s.eps"
set xlabel "Time (s)"
set ylabel "Messages"
set xrange [0:4600]
set yrange [0:%d]

plot """

datal = """'%s.dat' using 1:2 title "%s" with linespoints lw 2 lc %d%s \\
     """
ratep.write(headr % ("rates",3500))
loadp.write(headr % ("loads",16000))

for j in range(1,resId):
    ratep.write(datal % ("rates%d" % (j,),"Worker %d" % (j,),j,","))
    loadp.write(datal % ("loads%d" % (j,),"Worker %d" % (j,),j,","))

ratep.write(datal % ("rates%d" % (resId,),"Worker %d" % (resId,),resId,","))
ratep.write(datal % ("prod","Production",resId+1,";"))
loadp.write(datal % ("loads%d" % (resId,),"Worker %d" % (resId,),resId,";"))

ratep.close()
loadp.close()



        
