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

#include "Properties.H"
#include "XStream.H"
#include "BaseTestCase.H"

int main(int argc, char* argv[]) {
  BaseTestCase::startTest(argv);
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


  BaseTestCase::assertEquals(v1,p->getBooleanProperty("boolean"));
  /*
  if (v1 != p->getBooleanProperty("boolean")) {
    printf("Boolean NOK1\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v2,p->getByteProperty("byte"));
  /*
  if (v2 != p->getByteProperty("byte")) {
    printf("Byte NOK1\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v3,p->getDoubleProperty("double"));
  /*
  if (v3 != p->getDoubleProperty("double")) {
    printf("Double NOK1\n");
    exit(-1);
    }*/

  BaseTestCase::assertEquals(v4,p->getFloatProperty("float"));
  /*
  if (v4 != p->getFloatProperty("float")) {
    printf("Float NOK1\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v5,p->getIntProperty("int"));
  /*
  if (v5 != p->getIntProperty("int")) {
    printf("Int NOK1\n");
    exit(-1);
  }
  */
  BaseTestCase::assertEquals(v6,p->getLongProperty("long"));
  /*
  if (v6 != p->getLongProperty("long")) {
    printf("Boolean NOK1");
    exit(-1);
  }
  */
  BaseTestCase::assertEquals(v7,p->getShortProperty("short"));
  /*
  if (v7 != p->getShortProperty("short")) {
    printf("Short NOK1\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v8,p->getStringProperty("string"));
  /*
  if (strcmp(v8, p->getStringProperty("string")) != 0) {
    printf("String NOK1: \"%s\" != \"%s\"\n", v8, p->getStringProperty("string"));
    exit(-1);
  }
  */

  printf("Get/Set OK\n");

  BaseTestCase::assertEquals("true",p->getStringProperty("boolean"));
  /*
  if (strcmp(p->getStringProperty("boolean"), "true") != 0) {
    printf("Boolean -> String NOK\n");
    exit(-1);
  }
  */

  printf("Get String properties OK\n");

  OutputStream *os = new OutputStream();
  try {
    os->writeProperties(p);
  } catch (NotYetImplementedException exc) {
    printf("Exception\n");
    exit(-1);
  }
  int size1 = os->size();
  int ofd = open("toto", O_WRONLY|O_CREAT,0644);
  os->writeTo(ofd);
  close(ofd);

  int ifd = open("toto", O_RDONLY);
  int size2 = lseek(ifd, 0, SEEK_END);
    
  if (size2 != (size1 +4)) {
    BaseTestCase::error(new Exception("File size error"));
    printf("File size NOK: %d, %d\n", size1, size2);
    BaseTestCase::endTest();
    exit(-1);
  }
  
  lseek(ifd, 0, SEEK_SET);
  InputStream *is = new InputStream();
  is->readFrom(ifd);
  Properties *p1 = is->readProperties();
  close(ifd);
  unlink("toto");
  
  
  if (p1 == (Properties*) NULL) {
    BaseTestCase::error(new Exception("p1 is NULL"));
    printf("Properties NULL\n");
    BaseTestCase::endTest();
    exit(-1);
  }

  
  BaseTestCase::assertEquals(v1,p1->getBooleanProperty("boolean"));
  /*
  if (v1 != p1->getBooleanProperty("boolean")) {
    printf("Boolean NOK2\n");
    exit(-1);
  }
  */



   BaseTestCase::assertEquals(v2,p1->getByteProperty("byte"));
   /*
  if (v2 != p1->getByteProperty("byte")) {
    printf("Byte NOK2\n");
    exit(-1);
  }
   */
  double xv3 = p1->getDoubleProperty("double");
  float xv4 = p1->getFloatProperty("float");


  BaseTestCase::assertEquals(v3,p1->getDoubleProperty("double"));
  /*
  if (v3 != p1->getDoubleProperty("double")) {
    printf("Double NOK2\n");
    exit(-1);
  }
  */
  
  BaseTestCase::assertEquals(v4,p1->getFloatProperty("float"));
  /*
  if (v4 != p1->getFloatProperty("float")) {
    printf("Float NOK2\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v5,p1->getIntProperty("int"));
  /*
  if (v5 != p1->getIntProperty("int")) {
    printf("Int NOK2\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v6,p1->getLongProperty("long"));
  /*
  if (v6 != p1->getLongProperty("long")) {
    printf("Boolean NOK2");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v7,p1->getShortProperty("short"));
  /*
  if (v7 != p1->getShortProperty("short")) {
    printf("Short NOK2\n");
    exit(-1);
  }
  */

  BaseTestCase::assertEquals(v8,p1->getStringProperty("string"));
  /*
  if (strcmp(v8, p1->getStringProperty("string")) != 0) {
    printf("String NOK2: \"%s\"\n", p1->getStringProperty("string"));
    exit(-1);
  }
  */

  printf("Marshalling OK\n");
  BaseTestCase::endTest();
  exit(0);
}
