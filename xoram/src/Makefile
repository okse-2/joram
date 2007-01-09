###############################
# i86-cygwin
# CCOPT = -g
# LDFLAGS = -fPIC -shared
###############################
# i86-linux
CCOPT = -g -fPIC
LDFLAGS = -fPIC -shared
###############################

CC = g++ $(CCOPT)
LD = g++ $(CCOPT) $(LDFLAGS)

LIBXORAMOBJS = \
	Synchronized.o \
	Daemon.o \
	Properties.o \
	XStream.o \
	AbstractMessage.o \
	Message.o \
	Xoram.o

TESTS = \
	tests/base

clean:
	rm -f *.o *~ libxoram.so
	for TD in $(TESTS);\
		do cd $$TD; make clean ; cd - ; done

xoram: libxoram.so

libxoram.so:	$(LIBXORAMOBJS)
		$(LD) -o libxoram.so $(LIBXORAMOBJS)

tests:	libxoram.so
	for TD in $(TESTS);\
		do cd $$TD; make tests ; cd - ; done

.C.o:
		$(CC) -o $*.o -c $*.C
