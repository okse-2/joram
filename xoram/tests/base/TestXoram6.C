#include <unistd.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

int main(int argc, char *argv[]) {
  try {
    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess = cnx->createSession();
    Queue* queue = new Queue("#0.0.1026", "queue");
    Topic* topic = new Topic("#0.0.1027", "topic");
    MessageProducer* prod1 = sess->createProducer(queue);
    MessageProducer* prod2 = sess->createProducer(topic);
    MessageConsumer* cons1 = sess->createConsumer(queue);
    MessageConsumer* cons2 = sess->createConsumer(topic);

    Message* msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    Message* msg2 = sess->createMessage();
    prod2->send(msg2);
    printf("##### Message sent on topic: %s\n", msg2->getMessageID());

    Message* msg = cons1->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    msg = cons2->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    msg = cons1->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    msg = cons1->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    msg = cons1->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    msg = cons1->receive();
    printf("##### Message received: %s\n", msg->getMessageID());

    cnx->close();
  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
  } catch (...) {
    printf("##### exception\n");
  }
  printf("##### bye\n");
}
