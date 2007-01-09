#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "Daemon.H"

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
}
