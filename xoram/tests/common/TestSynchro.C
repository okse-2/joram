#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "Synchronized.H"

class TestSynchro : Synchronized {
 private:
  int count;

 public:
  TestSynchro() : Synchronized() {
    count = 0;
  }

  ~TestSynchro() {}


  void method(int pid) {
    sync_begin();   
    printf("%d - start method\n", pid);

    count += 1;
    while ((count % 3) != 0) {
      printf("%d - before wait\n", pid);
      wait();
    }
    printf("%d - notify\n", pid);
    notify();

    printf("%d - end method\n", pid);
    sync_end();
  }

  void method2() {
    sync_begin();   
    printf("start method\n");

    printf("before wait\n");
    wait(3000);
    printf("after wait\n");


    printf("end method\n");
    sync_end();
  }
};

TestSynchro* obj;

void *test(void *id) {
  int i;

  for (i=0; i<10; i++) {
    obj->method((int) id);
    sleep(random() %10);
  }
  pthread_exit(NULL);
}

void *test2() {
  int i;

  for (i=0; i<10; i++) {
    obj->method2();
  }
}

int main (int argc, char *argv[]) {
  int i, rc;
  pthread_t threads[3];
  int ids[3] = {0,1,2};
  pthread_attr_t attr;

  obj = new TestSynchro();

  printf("create threads\n");

  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
  pthread_create(&threads[0], &attr, test, (void*) &ids[0]);
  pthread_create(&threads[1], &attr, test, (void*) &ids[1]);
  pthread_create(&threads[2], &attr, test, (void*) &ids[2]);

  /* Wait for all threads to complete */
  printf("wait threads\n");
  for (i=0; i<3; i++) {
    pthread_join(threads[i], NULL);
  }
  printf("ends\n");

  test2();

  /* Clean up and exit */
  pthread_attr_destroy(&attr);

  delete obj;

  pthread_exit(NULL);
}
