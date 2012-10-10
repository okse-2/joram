include ../Makefile.global

clean:
	rm -f *.exe *.o *~
	rm -f testdaemon testsynchro testvector testxqueue testxstream testproperties

tests: testdaemon testsynchro testvector testxqueue testxstream testproperties

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

.C.o:
		$(CC) -o $*.o -c $*.C
