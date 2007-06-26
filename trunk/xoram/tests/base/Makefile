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

# XORAMOBJS= \
# 	../../src/AbstractMessage.o \
# 	../../src/Daemon.o \
# 	../../src/Message.o \
# 	../../src/Properties.o \
# 	../../src/Synchronized.o \
# 	../../src/XStream.o \
# 	../../src/Xoram.o

clean:
	rm -f *.exe *.o *~ libxoram.so

tests: test1 test2 test3 test4 test5 test6 test7 test8 

test1:	TestXoram1.o $(XORAMOBJS)
	$(LD) -o test1 TestXoram1.o -L$(LIBDIR) -lxoram

test2:	TestXoram2.o 
	$(LD) -o test2 TestXoram2.o -L$(LIBDIR) -lxoram

test3:	TestXoram3.o 
	$(LD) -o test3 TestXoram3.o -L$(LIBDIR) -lxoram

test4:	TestXoram4.o 
	$(LD) -o test4 TestXoram4.o -L$(LIBDIR) -lxoram

test5:	TestXoram5.o 
	$(LD) -o test5 TestXoram5.o -L$(LIBDIR) -lxoram

test6:	TestXoram6.o 
	$(LD) -o test6 TestXoram6.o -L$(LIBDIR) -lxoram

test7:	TestXoram7.o 
	$(LD) -o test7 TestXoram7.o -L$(LIBDIR) -lxoram

test8:  TestXoram8.o
	$(LD) -o test8 TestXoram8.o -L$(LIBDIR) -lxoram

.C.o:
		$(CC) -o $*.o -c $*.C
