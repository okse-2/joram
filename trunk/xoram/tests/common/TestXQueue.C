#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "XQueue.H"
#include "Daemon.H"

class Daemon1 : public Daemon {
 public:
  XQueue<char>* queue;

  Daemon1() : Daemon() {
  }

  void run() {
    queue->push("abc");
    queue->push("def");
    printf("sleep 5\n");
    sleep(5);
    queue->push("ghij");
    queue->push("klm");
    queue->push("nop");
    queue->push("qr");
    queue->push("st");
    queue->push("uv");
    printf("sleep 10\n");
    sleep(10);
    queue->push("wx");
    queue->push("yz");
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
      printf("%d -> %s\n", i, str);
      queue->pop();
      sleep(1);
    }
  }

  void close() {}
};

int main (int argc, char *argv[]) {
  XQueue<char>* queue = new XQueue<char>();
  Daemon1* d1 = new Daemon1();
  d1->queue = queue;
  Daemon2* d2 = new Daemon2();
  d2->queue = queue;

  d1->start();
  d2->start();

  d1->join();
  d2->join();
}
 
