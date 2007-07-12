#include <unistd.h>
#include <stdlib.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"
#include "Types.H"
#include "XoramAdmin.H"
#include "AbstractAdminMessage.H"

int main(int argc, char *argv[]) {

    boolean prod = TRUE;
    boolean cons = TRUE;
   
    if (argc > 1) {
      if (strcmp(argv[1],"consumer") == 0) {
        cons = TRUE;
        prod = FALSE;
      }
      if (strcmp(argv[1],"producer") == 0) {
        prod = TRUE;
        cons = FALSE;
      }
    }

  try {

    // create Admin and connect
    XoramAdmin* admin = new XoramAdmin();
    admin->connect("root", "root", 60);

    // create destination
    Queue* queue = admin->createQueue("queue");
    printf("queue->getUID() = %s, queue->getName() = %s\n",queue->getUID(), queue->getName());

    // set right
    admin->setFreeReading(queue);
    admin->setFreeWriting(queue);

    // create "anonymous" user
    admin->createUser("anonymous", "anonymous");

    admin->disconnect();

    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess1 = cnx->createSession();
    MessageProducer* prod1 = sess1->createProducer(queue);
    MessageConsumer* cons1 = sess1->createConsumer(queue);

    int nbMessage = 1000;

    char* name =  "prop_name";
    char* value = "my property";
    char* keyInt =  "prop_int";
    
    printf("value = %s\n", value);

    Message* msg1 = NULL;
    Message* msg = NULL;   

    printf("prod = %x, cons = %x\n",prod,cons);

    for (int i = 0; i < nbMessage; i++) {
      if (prod == TRUE) {
        msg1 = sess1->createMessage();
        msg1->setStringProperty(name, value);
        msg1->setIntProperty(keyInt, i);
    
        prod1->send(msg1);
        if (i % 100 == 1)
          printf("##### Message sent on queue: %s, prop = %s, i = %i\n", msg1->getMessageID(), msg1->getStringProperty(name), msg1->getIntProperty(keyInt));
        delete msg1;
      }

      if (cons == TRUE) {
        msg = cons1->receive();
        if (i % 100 == 1)
          printf("##### Message received: %s, prop = %s, i = %i\n", msg->getMessageID(), msg->getStringProperty(name), msg->getIntProperty(keyInt));
          delete msg;
      }
    }

    sess1->close();
    cnx->close();

    //delete sess1;
    //delete cnx;
    delete queue;
    delete cf;

  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
  } catch (...) {
    printf("##### exception\n");
  }
  printf("##### bye\n");
}
