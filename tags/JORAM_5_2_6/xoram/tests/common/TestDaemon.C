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

#include "Daemon.H"
#include "BaseTestCase.H"

class TestDaemon : public Daemon {
 private:
  char* name;

 public:
  TestDaemon(char* name) : Daemon() {
    this->name = name;
  }

  ~TestDaemon() {}

  void run() {
    int i = 0;
    while (running) {
      printf("%s counter=%d\n", name, i++);
      sleep(1);
    }
    printf("running false\n");
  }

  void close() {
  }
};

TestDaemon** obj;

int main (int argc, char *argv[]) {
  try{
    BaseTestCase::startTest(argv);
    obj = new TestDaemon* [3];
    obj[0] = new TestDaemon("D1");
    obj[1] = new TestDaemon("D2");
    obj[2] = new TestDaemon("D3");
    
    printf("created\n");
    
    printf("start D1\n");
    obj[0]->start();
    sleep(5);
    printf("start D2\n");
    obj[1]->start();
    sleep(5);
    printf("start D3\n");
    obj[2]->start();
    
    
    printf("stop D1\n");
    obj[0]->stop();
    sleep(5);
    printf("stop D2\n");
    obj[1]->stop();
    sleep(5);
    printf("stop D3\n");
    obj[2]->stop();

  }catch(Exception exc){
    printf("exception - %s \n",exc.getMessage());
    BaseTestCase::error(&exc);
  }
  BaseTestCase::endTest();
}
