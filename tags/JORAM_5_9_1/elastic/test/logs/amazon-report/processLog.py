#!/usr/bin/python

import sys
from datetime import datetime

OF = len("monitoring");		# Offset to get worker ID
TF = "%Y/%m/%d %H:%M:%S,%f"	# Time format
SP = ";"			# CSV files separator
MAX_RND = 4900			# Maximum number of lines to read
PERIOD = 1			# In seconds

# Gets the ID from a file name
def getId():
	start = fname.find("monitoring")+OF
	end = fname.find(".csv")
	return int(fname[start:end])

# Gets the the number of rounds since t0
def getRound():
	tc = datetime.strptime(cells[datei],TF)
	tr = tc - t0
	return int(round(tr.total_seconds() / PERIOD))


# Main

# Init production
prod = [0] * MAX_RND
nbrw = [0] * MAX_RND
suml = [0] * MAX_RND
maxl = [0] * MAX_RND

# Init indexes and t0
datei = 0
recvi = 2
loadi = 4
length = 6
for i in range(1,len(sys.argv)):
	fname = sys.argv[i]
	wId = getId()
	
	# Looking for worker 1
	if wId == 1:
		csv = open(fname,"r")
		cells = csv.readline().split(SP)
		while len(cells) != length:
			cells = csv.readline().split(SP)
		t0 = datetime.strptime(cells[datei],TF)
		csv.close()
        	break

resId = 0
for i in range(1,len(sys.argv)):
	fname = sys.argv[i]
	wId = getId()
	if wId > 100:
		continue # skip producers

	csv = open(fname,"r")
	lines = csv.readlines()

	# Process data
	resId += 1
	ratef = open("rates/rates%d.dat" % resId,"w")
	loadf = open("loads/loads%d.dat" % resId,"w")
	lrecv = 0
	for j in range(1,min(MAX_RND,len(lines))):
		cells = lines[j].split(SP)
		if len(cells) != length:
        		continue

		rnd = getRound()
		rate = int(cells[recvi]) - lrecv
		lrecv = int(cells[recvi])
		load = int(cells[loadi])

		prod[rnd] += rate
		nbrw[rnd] += 1
		suml[rnd] += load
		if load > maxl[rnd]:
			maxl[rnd] = load
	csv.close()

# Correct nbrw
chg = False
for i in range(2,MAX_RND):
	if nbrw[i] != nbrw[i-1]:
		if chg:
			nbrw[i-1] = nbrw[i-2]
		chg = True
	else:
		chg = False

# Sliding average on maxl
W = 10
avgl = [0] * MAX_RND
for i in range((W - 1) / 2,MAX_RND - W / 2):
	avgl[i] = 0
	s = 0
	for j in range(i - (W - 1) / 2, i + W / 2):
		s += maxl[j]
	avgl[i] = s / W

# Create prod, nbrw and suml datafiles.
prodf = open("rates/prod.dat","w")
nbrwf = open("plus/nbrw.dat","w")
sumlf = open("plus/suml.dat","w")
maxlf = open("plus/maxl.dat","w")

for i in range(len(prod)):
	prodf.write("%d\t%d\n" % (i*PERIOD,prod[i]))
	nbrwf.write("%d\t%d\n" % (i*PERIOD,nbrw[i]))
	sumlf.write("%d\t%d\n" % (i*PERIOD,suml[i]))
	maxlf.write("%d\t%d\n" % (i*PERIOD,maxl[i]))

prodf.close()
nbrwf.close()
sumlf.close()
maxlf.close()
