LIBDIR = ../../src
INCDIR = ../../src

###############################
# i86-cygwin
# CCOPT = -g -I$(INCDIR)
# LDFLAGS = -g
# LIBXORAM = libxoram.dll
###############################
# i86-linux
CCOPT = -g -fPIC -I$(INCDIR)
LDFLAGS = -lrt
LIBXORAM = libxoram.so
###############################

CC = gcc $(CCOPT) 
LD = gcc $(CCOPT) $(LDFLAGS)

clean:
	rm -f *.exe *.o *~ ${LIBXORAM}
	rm -f test1

tests: test1

test1:	TestXoramWrapper.o
	$(LD) -o test1 TestXoramWrapper.o -L$(LIBDIR) -lxoram

.c.o:
	$(CC) -o $*.o -c $*.c
