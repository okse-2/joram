LIBDIR = ../../src
INCDIR = ../../src

###############################
# i86-cygwin
CCOPT = -g -I../../src
LDFLAGS = -g
LIBXORAM = libxoram.dll
###############################
# i86-linux
#CCOPT = -g -I../../src
#LDFLAGS = -lrt
#LIBXORAM = libxoram.so
###############################

CC = gcc $(CCOPT) 
LD = gcc $(CCOPT) $(LDFLAGS)

clean:
	rm -f *.exe *.o *~ ${LIBXORAM}

tests: test1
	cp $(LIBDIR)/${LIBXORAM} .

test1:	TestXoramWrapper.o
	$(LD) -o test1 TestXoramWrapper.o -L$(LIBDIR) -lxoram

.c.o:
	$(CC) -o $*.o -c $*.c
