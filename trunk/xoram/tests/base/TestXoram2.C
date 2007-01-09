#include <unistd.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

int main (int argc, char *argv[]) {
  try {
    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess = cnx->createSession();
    Queue* queue = new Queue("#0.0.1026", "queue");
    MessageProducer* prod = sess->createProducer(queue);
    MessageConsumer* cons = sess->createConsumer(queue);
    Message* msg1 = sess->createMessage();
    prod->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());
    Message* msg2 = cons->receive();
    printf("##### Message received: %s\n", msg1->getMessageID());
    cnx->close();
  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
  } catch (...) {
    printf("##### exception\n");
  }
  printf("##### bye\n");
}
