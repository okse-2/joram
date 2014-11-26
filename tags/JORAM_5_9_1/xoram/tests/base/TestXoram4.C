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

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"
#include "XoramAdmin.H"
#include "AbstractAdminMessage.H"
#include "BaseTestCase.H"

class MyListener : public MessageListener {
 public:
  virtual void onMessage(Message* msg) {
    printf("##### Message received: %s\n", msg->getMessageID());
  }
};

int main(int argc, char *argv[]) {
  try {
    BaseTestCase::startTest(argv);

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
    admin->disconnect();

    printf("##### ##### trace0\n");

    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    //Topic* topic = new Topic("#0.0.1027", "topic");
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
  } catch (NotYetImplementedException exc) {
    printf("##### Not yet implemented\n", exc.getMessage());
  } catch (Exception exc) {
    printf("##### exception - %s\n", exc.getMessage());
    BaseTestCase::error(&exc);
  } catch (...) {
    printf("##### exception\n");
    BaseTestCase::error(new Exception(" catch ..., unknown exception "));
  }
  printf("##### bye\n");
  BaseTestCase::endTest();
}
