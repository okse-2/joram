#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

/* Pour open, read, etc. */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "Properties.H"
#include "XStream.H"

int main(int argc, char* argv[]) {
  boolean v1 = TRUE;
  byte v2 = (byte) 5;
  double v3 = 3.1415927;
  float v4 = 3.1415927;
  int v5 = 15;
  long long v6 = 1234567890123LL;
  short v7 = (short) 15000;
  char* v8 = "abcde";

  Properties *p = new Properties();
  p->setBooleanProperty("boolean", v1);
  p->setByteProperty("byte", v2);
  p->setDoubleProperty("double", v3);
  p->setFloatProperty("float", v4);
  p->setIntProperty("int", v5);
  p->setLongProperty("long", v6);
  p->setShortProperty("short", v7);
  p->setStringProperty("string", v8);

  if (v1 != p->getBooleanProperty("boolean")) {
    printf("Boolean NOK1\n");
    exit(-1);
  }

  if (v2 != p->getByteProperty("byte")) {
    printf("Byte NOK1\n");
    exit(-1);
  }

  if (v3 != p->getDoubleProperty("double")) {
    printf("Double NOK1\n");
    exit(-1);
  }

  if (v4 != p->getFloatProperty("float")) {
    printf("Float NOK1\n");
    exit(-1);
  }

  if (v5 != p->getIntProperty("int")) {
    printf("Int NOK1\n");
    exit(-1);
  }

  if (v6 != p->getLongProperty("long")) {
    printf("Boolean NOK1");
    exit(-1);
  }

  if (v7 != p->getShortProperty("short")) {
    printf("Short NOK1\n");
    exit(-1);
  }
  
  if (strcmp(v8, p->getStringProperty("string")) != 0) {
    printf("String NOK1: \"%s\" != \"%s\"\n", v8, p->getStringProperty("string"));
    exit(-1);
  }

  printf("Get/Set OK\n");

  if (strcmp(p->getStringProperty("boolean"), "true") != 0) {
    printf("Boolean -> String NOK\n");
    exit(-1);
  }

  printf("Get String properties OK\n");

  OutputStream *os = new OutputStream();
  try {
    os->writeProperties(p);
  } catch (NotYetImplementedException exc) {
    printf("Exception\n");
    exit(-1);
  }
  int size1 = os->size();
  int ofd = open("toto", O_WRONLY|O_CREAT);
  os->writeTo(ofd);
  close(ofd);

  int ifd = open("toto", O_RDONLY);
  int size2 = lseek(ifd, 0, SEEK_END);
  if (size2 != (size1 +4)) {
    printf("File size NOK: %d, %d\n", size1, size2);
    exit(-1);
  }

  lseek(ifd, 0, SEEK_SET);
  InputStream *is = new InputStream();
  is->readFrom(ifd);
  Properties *p1 = is->readProperties();

  close(ifd);
  unlink("toto");

  if (p1 == (Properties*) NULL) {
    printf("Properties NULL\n");
    exit(-1);
  }

  if (v1 != p1->getBooleanProperty("boolean")) {
    printf("Boolean NOK2\n");
    exit(-1);
  }

  if (v2 != p1->getByteProperty("byte")) {
    printf("Byte NOK2\n");
    exit(-1);
  }

  double xv3 = p1->getDoubleProperty("double");
  float xv4 = p1->getFloatProperty("float");

  if (v3 != p1->getDoubleProperty("double")) {
    printf("Double NOK2\n");
    exit(-1);
  }

  if (v4 != p1->getFloatProperty("float")) {
    printf("Float NOK2\n");
    exit(-1);
  }

  if (v5 != p1->getIntProperty("int")) {
    printf("Int NOK2\n");
    exit(-1);
  }

  if (v6 != p1->getLongProperty("long")) {
    printf("Boolean NOK2");
    exit(-1);
  }

  if (v7 != p1->getShortProperty("short")) {
    printf("Short NOK2\n");
    exit(-1);
  }
  
  if (strcmp(v8, p1->getStringProperty("string")) != 0) {
    printf("String NOK2: \"%s\"\n", p1->getStringProperty("string"));
    exit(-1);
  }

  printf("Marshalling OK\n");

  exit(0);
}
