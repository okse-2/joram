LIBDIR = ../../src
INCDIR = ../../src

###############################
# i86-cygwin
#CCOPT = -g
#LDFLAGS = -shared
#LIBXORAM = libxoram.dll
###############################
# i86-linux
CCOPT = -g -fPIC
LDFLAGS = -fPIC -shared
LIBXORAM = libxoram.so
###############################

CC = g++ $(CCOPT)
LD = g++ $(CCOPT) $(LDFLAGS)

TESTS = ../tests/common ../tests/base ../tests/wrapper ../tests/perfs

LIBXORAMOBJS = \
	Synchronized.o \
	Daemon.o \
	Properties.o \
	XStream.o \
	AbstractMessage.o \
	Message.o \
	Xoram.o \
	XoramWrapper.o \
	BaseTestCase.o \
	AbstractAdminMessage.o \
	XoramAdmin.o

clean:
	rm -f *.o *~ $(LIBXORAM)
	for TD in $(TESTS);\
		do cd $$TD; make clean ; cd - ; done

xoram: $(LIBXORAM)

$(LIBXORAM):	$(LIBXORAMOBJS)
		$(LD) -o $(LIBXORAM) $(LIBXORAMOBJS)

tests:	$(LIBXORAM)
	for TD in $(TESTS);\
		do cd $$TD; make tests ; cd - ; done

.C.o:
		$(CC) -o $*.o -c $*.C
