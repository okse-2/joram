LIBDIR = ../../src
INCDIR = ../../src

###############################
# i86-cygwin
#CCOPT = -g -I$(INCDIR)
#LDFLAGS = -g
#LIBXORAM = libxoram.dll
###############################
# i86-linux
 CCOPT = -g -I../../src
 LDFLAGS = -lrt
 LIBXORAM = libxoram.so
###############################

CC = g++ $(CCOPT)
LD = g++ $(CCOPT) $(LDFLAGS)

clean:
	rm -f *.exe *.o *~ 

tests: testdaemon testsynchro testvector testxqueue testxstream testproperties test

testdaemon:	TestDaemon.o $(XORAMOBJS)
	$(LD) -o testdaemon TestDaemon.o -L$(LIBDIR) -lxoram

testsynchro:	TestSynchro.o 
	$(LD) -o testsynchro TestSynchro.o -L$(LIBDIR) -lxoram

testvector:	TestVector.o 
	$(LD) -o testvector TestVector.o -L$(LIBDIR) -lxoram

testxqueue:	TestXQueue.o 
	$(LD) -o testxqueue TestXQueue.o -L$(LIBDIR) -lxoram

testxstream:	TestXStream.o 
	$(LD) -o testxstream TestXStream.o -L$(LIBDIR) -lxoram

testproperties:	TestProperties.o 
	$(LD) -o testproperties TestProperties.o -L$(LIBDIR) -lxoram

test: Test.o
	$(LD) -o test Test.o -L$(LIBDIR) -lxoram
	
.C.o:
		$(CC) -o $*.o -c $*.C
