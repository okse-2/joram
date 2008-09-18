/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

/* Pour open, read, etc. */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "XStream.H"
#include "BaseTestCase.H"

class Test1 : Streamable  {
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

  boolean equals(Test1 *o) {
    if (field1 != o->field1) return FALSE;
    if (field2 != o->field2) return FALSE;
    if (strcmp(field3, o->field3) != 0) return FALSE;

    return TRUE;
  }
};

int main(int argc, char* argv[]) {
  BaseTestCase::startTest(argv);
  byte v2 = (byte) 5;
  double v3 = 3.1415927;
  float v4 = 3.1415927;
  int v5 = 15;
  long long v6 = 1234567890123LL;
  short v7 = (short) 15000;
  char* v8 = "abcde";
  char* v9 = "abcde";
  int v10 = -128;

  OutputStream *os = new OutputStream();

  os->writeBoolean(TRUE);
  os->writeBoolean(FALSE);
  os->writeByte(v2);
  os->writeDouble(v3);
  os->writeFloat(v4);
  os->writeInt(v5);
  os->writeLong(v6);
  os->writeShort(v7);
  os->writeString(v8);
  os->writeString(v9);
  os->writeInt(v10);

  int size1 = os->size();
  int ofd = open("toto", O_WRONLY|O_CREAT,0644);

  os->writeTo(ofd);
  close(ofd);
  int ifd = open("toto", O_RDONLY);
  int size2 = lseek(ifd, 0, SEEK_END);
  BaseTestCase::assertEquals(size2,(size1+4));
  /*
  if (size2 != (size1 +4)) {
    printf("File size NOK: %d, %d\n", size1, size2);
    exit(-1);
  }
  */

  lseek(ifd, 0, SEEK_SET);
  InputStream *is = new InputStream();
  is->readFrom(ifd);

  boolean b;
  if (is->readBoolean(&b) !=0)
    BaseTestCase::error(new Exception("can't read boolean"));
  BaseTestCase::assertEquals((boolean)TRUE,b);
  /*
  if ((is->readBoolean(&b) !=0) || (b != TRUE)) {
    printf("Boolean NOK\n");
    exit(-1);
  }
  */

  if (is->readBoolean(&b) !=0)
    BaseTestCase::error(new Exception("can't read boolean"));
  BaseTestCase::assertEquals((boolean)FALSE,b);
  /*
  if ((is->readBoolean(&b) !=0) || (b != FALSE)) {
    printf("Boolean NOK\n");
    exit(-1);
  }
  */

  byte o;
  if (is->readByte(&o) !=0)
    BaseTestCase::error(new Exception("can't read Byte"));
  BaseTestCase::assertEquals(v2,o);
  /*
  if ((is->readByte(&o) !=0) || (o != v2)) {
    printf("Byte NOK\n");
    exit(-1);
  }
  */

  double d;
  if (is->readDouble(&d) !=0)
    BaseTestCase::error(new Exception("can't read double"));
  BaseTestCase::assertEquals(v3,d);
  /*
  if ((is->readDouble(&d) !=0) || (d != v3)) {
    printf("Double NOK\n");
    exit(-1);
  }
  */

  float f;
  if (is->readFloat(&f) !=0)
    BaseTestCase::error(new Exception("can't read float"));
  BaseTestCase::assertEquals(v4,f);
  /*
  if ((is->readFloat(&f) !=0) || (f != v4)) {
    printf("Float NOK\n");
    exit(-1);
  }
  */

  int i;
  if (is->readInt(&i) !=0)
    BaseTestCase::error(new Exception("can't read int"));
  BaseTestCase::assertEquals(v5,i);
  /*
  if ((is->readInt(&i) !=0) || (i != v5)) {
    printf("Int NOK\n");
    exit(-1);
  }
  */

  long long l;
  if (is->readLong(&l) !=0)
    BaseTestCase::error(new Exception("can't read long"));
  BaseTestCase::assertEquals(v6,l);
  /*
  if ((is->readLong(&l) !=0) || (l != v6)) {
    printf("Long NOK\n");
    exit(-1);
  }
  */

  short s;
  if (is->readShort(&s)!=0)
     BaseTestCase::error(new Exception("can't read short"));
  BaseTestCase::assertEquals(v7,s);
  /*
  if ((is->readShort(&s) !=0) || (s != v7)) {
    printf("Short NOK\n");
    exit(-1);
  }
  */

  char *str;
  if (is->readString(&str) !=0)
    BaseTestCase::error(new Exception("can't read char*"));
  BaseTestCase::assertEquals(v8,str);
  /*
  if ((is->readString(&str) !=0) || (strcmp(str, v8) != 0)) {
    printf("String NOK\n");
    exit(-1);
  }
  */
  if (is->readString(&str) !=0)
    BaseTestCase::error(new Exception("can't read char*"));
  BaseTestCase::assertEquals(v9,str);
  /*
  if ((is->readString(&str) !=0) || (strcmp(str, v9) != 0)) {
    printf("String NOK\n");
    exit(-1);
  }
  */
  if (is->readInt(&i) !=0)
    BaseTestCase::error(new Exception("can't read int"));
  BaseTestCase::assertEquals(v10,i);
  /*
  if ((is->readInt(&i) !=0) || (i != v10)) {
    printf("Int NOK\n");
    exit(-1);
  }
  */
  int pos =unlink("toto");
  close(ifd);
  
  printf("Test1 OK\n");
 
  os = new OutputStream();
  Test1 *t1 = new Test1(1234567, (short) 12, "azertyuiopX");
  t1->writeTo(os);
  size1 = os->size();
 
  ofd =open("toto", O_WRONLY|O_CREAT,0644);
  os->writeTo(ofd);
  close(ofd);
  
  ifd = open("toto", O_RDONLY);
  size2 = lseek(ifd, 0, SEEK_END);
  BaseTestCase::assertEquals(size2,(size1+4));
  /*
  if (size2 != (size1 +4)) {
    printf("File size NOK: %d, %d\n", size1, size2);
    exit(-1);
  }
  */
  
  lseek(ifd, 0, SEEK_SET);
  is = new InputStream();
  is->readFrom(ifd);

  Test1 *t2 = new Test1();
  t2->readFrom(is);

  BaseTestCase::assertTrue(t1->equals(t2) == TRUE);
  /*
  if (t1->equals(t2) != TRUE) {
    printf("Object NOK\n");
    exit(-1);
  }
  */

  close(ifd);
  unlink("toto");

  printf("Test2 OK\n");


  BaseTestCase::endTest();
  exit(0);
}
