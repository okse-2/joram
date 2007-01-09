#include <unistd.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

int main (int argc, char *argv[]) {
  try {
    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    printf("##### Cnx state(1): %d\n", cnx->isStopped());
    cnx->start();
    printf("##### Cnx state(0): %d\n", cnx->isStopped());
    cnx->stop();
    printf("##### Cnx state(1): %d\n", cnx->isStopped());
    cnx->start();
    printf("##### Cnx state(0): %d\n", cnx->isStopped());
    cnx->close();
  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
  } catch (...) {
    printf("##### exception\n");
  }
  printf("##### bye\n");
}
