#!/usr/bin/python

import os, sys

fl = 10 # Palier

java = open("Load.java","w")
java.write("""public class Load {
	public static int computeLoad(int round) {""")

pl = 0
pr = 0
for i in range(1,len(sys.argv)):
	cl = int(sys.argv[i])
	cr = pr + abs(cl-pl)
        java.write("""
                if (round < %d)
                        return %d + (%d) * (round - %d);
		if (round < %d)
			return %d;""" % (cr,pl,cmp(cl,pl),pr,cr+fl,cl))
	pl = cl
	pr = cr + fl

cr = pr + pl
cl = 0
java.write("""
                if (round < %d)
                        return %d + (%d) * (round - %d);
		return 0;
	}

	public static void main(String argv[]) {
		for (int  i = 0; i < %d; i++)
			System.out.println(computeLoad(i));
	}
}
""" % (cr,pl,cmp(cl,pl),pr,cr))
java.close()

os.system("cat Load.java")
os.system("javac Load.java")
os.system("java Load")
os.system("rm -f Load.java Load.class")
