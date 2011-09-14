LIBDIR = ../../src
INCDIR = ../../src

###############################
# i86-cygwin
#CCOPT = -g -I$(INCDIR)
#LDFLAGS = -g
#LIBXORAM = libxoram.dll
###############################
# i86-linux
CCOPT = -g -fPIC -I$(INCDIR)
LDFLAGS = -lrt
LIBXORAM = libxoram.so
###############################

CC = g++ $(CCOPT)
LD = g++ $(CCOPT) $(LDFLAGS)

clean:
	rm -f *.exe *.o *~
	rm -f test1 test2 test3 test4 test5

tests: test1 test2 test3 test4 test5

test1:	TestPerf1.o
	$(LD) -o test1 TestPerf1.o -L$(LIBDIR) -lxoram

test2:	TestPerf2.o
	$(LD) -o test2 TestPerf2.o -L$(LIBDIR) -lxoram

test3:	TestPerf3.o
	$(LD) -o test3 TestPerf3.o -L$(LIBDIR) -lxoram

test4:	TestPerf4.o
	$(LD) -o test4 TestPerf4.o -L$(LIBDIR) -lxoram

test5:	TestPerf5.o
	$(LD) -o test5 TestPerf5.o -L$(LIBDIR) -lxoram

.C.o:
		$(CC) -o $*.o -c $*.C
