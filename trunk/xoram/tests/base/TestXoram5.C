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

int main(int argc, char *argv[]) {
  try {
    BaseTestCase::startTest(argv);
    
    XoramAdmin* admin = new XoramAdmin();
    admin->connect("root", "root", 60);

    // create destination
    Queue* queue = admin->createQueue("queue");
    Topic* topic = admin->createTopic("topic");
    
    // set right
    admin->setFreeReading(queue);
    admin->setFreeWriting(queue);
    admin->setFreeReading(topic);
    admin->setFreeWriting(topic);

    // create "anonymous" user
    admin->createUser("anonymous", "anonymous");
    admin->disconnect();

    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess = cnx->createSession();
    //Queue* queue = new Queue("#0.0.1026", "queue");
    //Topic* topic = new Topic("#0.0.1027", "topic");
    MessageProducer* prod1 = sess->createProducer(queue);
    MessageProducer* prod2 = sess->createProducer(topic);
    MessageConsumer* cons1 = sess->createConsumer(queue);
    MessageConsumer* cons2 = sess->createConsumer(topic);

    Message* msg1 = sess->createMessage();
    prod1->send(msg1);
    //printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    Message* msg2 = sess->createMessage();
    prod2->send(msg2);
    //printf("##### Message sent on topic: %s\n", msg2->getMessageID());

    Message* msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg1->getMessageID(), msg->getMessageID());

    msg = cons2->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg2->getMessageID(), msg->getMessageID());

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
