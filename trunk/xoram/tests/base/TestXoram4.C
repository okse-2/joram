#include <unistd.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"

class MyListener : public MessageListener {
 public:
  virtual void onMessage(Message* msg) {
    printf("##### Message received: %s\n", msg->getMessageID());    
  }
};

int main(int argc, char *argv[]) {
  try {
    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    Topic* topic = new Topic("#0.0.1027", "topic");
    Session* sess1 = cnx->createSession();
    MessageProducer* prod = sess1->createProducer(topic);

    printf("##### ##### trace1\n");

    Session* sess2 = cnx->createSession();
    MessageConsumer* cons = sess2->createConsumer(topic);
    MessageListener* listener = new MyListener();
    cons->setMessageListener(listener);

    printf("##### ##### trace2\n");

    cnx->start();

    printf("##### ##### trace3\n");

    Message* msg1 = sess1->createMessage();
    prod->send(msg1);
    printf("##### Message sent on topic: %s\n", msg1->getMessageID());

    Message* msg2 = sess1->createMessage();
    prod->send(msg2);
    printf("##### Message sent on topic: %s\n", msg2->getMessageID());

    cnx->close();
  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
  } catch (...) {
    printf("##### exception\n");
  }
  printf("##### bye\n");
}
