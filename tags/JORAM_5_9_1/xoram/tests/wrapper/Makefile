include ../Makefile.global

clean:
	rm -f *.exe *.o *~ ${LIBXORAM}
	rm -f test1

tests: test1

test1:	TestXoramWrapper.o
	$(LD) -o test1 TestXoramWrapper.o -L$(LIBDIR) -lxoram

.c.o:
	$(CC) -o $*.o -c $*.c
