#include <unistd.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"
#include "XoramAdmin.H"
#include "AbstractAdminMessage.H"
#include "BaseTestCase.H"
int main(int argc, char *argv[]) {
  try {
    BaseTestCase::startTest(argv);
    // create Admin and connect
    XoramAdmin* admin = new XoramAdmin();
    admin->connect("root", "root", 60);

    // create destination
    Queue* queue = admin->createQueue("queue");
    printf("queue->getUID() = %s, queue->getName() = %s\n",queue->getUID(), queue->getName());
    Topic* topic = admin->createTopic("topic");
    printf("topic->getUID() = %s, topic->getName() = %s\n",topic->getUID(), topic->getName());

    // set right
    admin->setFreeReading(queue);
    admin->setFreeWriting(queue);
    admin->setFreeReading(topic);
    admin->setFreeWriting(topic);

    // create "anonymous" user
    admin->createUser("anonymous", "anonymous");

    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess = cnx->createSession();
    MessageProducer* prod1 = sess->createProducer(queue);
    MessageProducer* prod2 = sess->createProducer(topic);
    MessageConsumer* cons1 = sess->createConsumer(queue);
    MessageConsumer* cons2 = sess->createConsumer(topic);

    Message* msg1 = sess->createMessage();
    prod1->send(msg1);
    printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    Message* msg2 = sess->createMessage();
    prod2->send(msg2);
    printf("##### Message sent on topic: %s\n", msg2->getMessageID());
    
    Message* msg = cons1->receive();
    printf("##### Message received from queue: %s\n", msg->getMessageID());

    msg = cons2->receive();
    printf("##### Message received from tpoic: %s\n", msg->getMessageID());
    
    // delete User
    CreateUserReply* userReply = admin->createUser("removeUser", "removeUser");
    admin->deleteUser("removeUser",userReply->getProxId());

    // delete Queue and Topic
    printf("delete Queue %s\n", queue->getUID());
    admin->deleteDestination(queue->getUID());
    printf("delete Topic %s\n", topic->getUID());
    admin->deleteDestination(topic->getUID());

    admin->disconnect();

    cnx->close();

} catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
    BaseTestCase::error(&exc);
  } catch (...) {
    printf("##### exception\n");
    BaseTestCase::error(new Exception(" catch ..., unknown exception "));
  }
  printf("##### bye\n");
  BaseTestCase::endTest();
}


  
