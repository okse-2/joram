###############################
# i86-cygwin
CCOPT = -g -I../../src
LDFLAGS = 
LIBXORAM = libxoram.dll.a
###############################
# i86-linux
# CCOPT = -g -I../../src
# LDFLAGS = -lrt
###############################

CC = g++ $(CCOPT)
LD = g++ $(CCOPT) $(LDFLAGS)

XORAMOBJS= \
	../../src/AbstractMessage.o \
	../../src/Daemon.o \
	../../src/Message.o \
	../../src/Properties.o \
	../../src/Synchronized.o \
	../../src/XStream.o \
	../../src/Xoram.o

clean:
	rm -f *.exe *.o *~ libxoram.so

tests: test1 test2 test3 test4 test5 test6 test7

test1:	TestXoram1.o $(XORAMOBJS)
	$(LD) -o test1 TestXoram1.o $(XORAMOBJS)

test2:	TestXoram2.o ../../src/libxoram.so
	$(LD) -o test2 TestXoram2.o $(XORAMOBJS)

test3:	TestXoram3.o ../../src/libxoram.so
	$(LD) -o test3 TestXoram3.o $(XORAMOBJS)

test4:	TestXoram4.o ../../src/libxoram.so
	$(LD) -o test4 TestXoram4.o $(XORAMOBJS)

test5:	TestXoram5.o ../../src/libxoram.so
	$(LD) -o test5 TestXoram5.o $(XORAMOBJS)

test6:	TestXoram6.o ../../src/libxoram.so
	$(LD) -o test6 TestXoram6.o $(XORAMOBJS)

test7:	TestXoram7.o ../../src/libxoram.so
	$(LD) -o test7 TestXoram7.o $(XORAMOBJS)

.C.o:
		$(CC) -o $*.o -c $*.C
