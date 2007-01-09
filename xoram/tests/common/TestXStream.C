#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

/* Pour open, read, etc. */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "XStream.H"

class Test1 : Streamable {
 private:
  int field1;
  short field2;
  char* field3;

 public:
  Test1() {};

  Test1(int f1, short f2, char* f3) {
    field1 = f1;
    field2 = f2;
    field3 = f3;
  };

  void writeTo(OutputStream *os) throw (IOException) {
    if (os->writeInt(field1) == -1) throw IOException();
    if (os->writeShort(field2) == -1) throw IOException();
    if (os->writeString(field3) == -1) throw IOException();
  }

  void readFrom(InputStream *is) throw (IOException) {
    if (is->readInt(&field1) == -1) throw IOException();
    if (is->readShort(&field2) == -1) throw IOException();
    if (is->readString(&field3) == -1) throw IOException();
  }

  void print() {
    printf("(Test1 : field1=%d, field2=%hd, field3=%s)\n", field1, field2, field3);
  }
};

int main(int argc, char* argv[]) {
  if (argc != 2) exit(-1);

  char *test = argv[1];
  printf("test=%s\n", test);

  if (strcmp(test, "1") == 0) {
    OutputStream *os = new OutputStream();

    os->writeInt(12);
    os->writeInt(24);
/*     os->writeString("abcdeX"); */
/*     os->writeLong(13L); */
/*     os->writeShort((short) 1234); */
/*     os->writeString("abcdeabcdeabcdeabcdeabcdeY"); */

    int size = os->size();
    printf("size=%d\n", size);
    int fd = open("toto", O_WRONLY|O_CREAT);
    os->writeTo(fd);
  } else if (strcmp(test, "2") == 0) {
    int fd = open("toto", O_RDONLY);
    int size = lseek(fd, 0, SEEK_END);
    printf("size=%d\n", size);
    if (size <= 0) exit(-1);
    lseek(fd, 0, SEEK_SET);
    InputStream *is = new InputStream();
    is->readFrom(fd);

    int i;
    is->readInt(&i);
    printf("%d\n", i);
    is->readInt(&i);
    printf("%d\n", i);

/*     char *str; */
/*     is->readString(&str); */
/*     printf("%s\n", str); */

/*     long long l; */
/*     is->readLong(&l); */
/*     printf("%ld\n", l); */

/*     short s; */
/*     is->readShort(&s); */
/*     printf("%hd\n", s); */

/*     is->readString(&str); */
/*     printf("%s\n", str); */
/*   } else if (strcmp(test, "3") == 0) { */
/*     OutputStream *os = new OutputStream(); */
/*     Test1 *t1 = new Test1(1234567, (short) 12, "azertyuiopX"); */
/*     t1->writeTo(os); */
    
/*     int size = os->size(); */
/*     printf("size=%d\n", size); */
/*     int fd = open("toto", O_WRONLY|O_CREAT); */
/*     os->writeTo(fd); */
/*   } else if (strcmp(test, "4") == 0) { */
/*     int fd = open("toto", O_RDONLY); */
/*     int size = lseek(fd, 0, SEEK_END); */
/*     printf("size=%d\n", size); */
/*     if (size <= 0) exit(-1); */
/*     lseek(fd, 0, SEEK_SET); */
/*     byte* buffer = new byte[size]; */
/*     read(fd, (void*) buffer, size); */
/*     InputStream *is = new InputStream(buffer, size); */

/*     Test1 *t1 = new Test1(); */
/*     t1->readFrom(is); */
/*     t1->print(); */
  }

  exit(0);
}
