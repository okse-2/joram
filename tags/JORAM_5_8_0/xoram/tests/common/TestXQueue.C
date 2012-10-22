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
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "XQueue.H"
#include "Daemon.H"
#include "BaseTestCase.H"

class Daemon1 : public Daemon {
 public:
  XQueue<char>* queue;

  Daemon1() : Daemon() {
  }

  void run() {
    queue->push("abc1");
    queue->push("abc2");
    printf("sleep 5\n");
    sleep(5);
    queue->push("abc3");
    queue->push("abc4");
    queue->push("abc5");
    queue->push("abc6");
    queue->push("abc7");
    queue->push("abc8");
    printf("sleep 10\n");
    sleep(10);
    queue->push("abc9");
    queue->push("abc10");
    printf("stopping\n");
    queue->stop();
    printf("stopped\n");
  }

  void close() {}
};

class Daemon2 : public Daemon {
 public:
  XQueue<char>* queue;

  Daemon2() : Daemon() {
  }

  void run() {
    for (int i=0; i<10; i++) {
      char* str = queue->get();
     
      char expected[30]="abc";
      char buffer[3];
      snprintf(buffer, 3, "%d",i+1 );
      strcat(expected,buffer);
      BaseTestCase::assertEquals(expected,str);
     
      //printf("%d -> %s\n", i, str);
      queue->pop();
      sleep(1);
    }
  }

  void close() {}
};

int main (int argc, char *argv[]) {
  BaseTestCase::startTest(argv);
  XQueue<char>* queue = new XQueue<char>();
  Daemon1* d1 = new Daemon1();
  d1->queue = queue;
  Daemon2* d2 = new Daemon2();
  d2->queue = queue;

  d1->start();
  d2->start();

  d1->join();
  d2->join();
  BaseTestCase::endTest();
}
 
