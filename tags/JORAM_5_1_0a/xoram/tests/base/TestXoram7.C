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
    Session* sess1 = cnx->createSession();
    Session* sess2 = cnx->createSession();
    //Queue* queue = new Queue("#0.0.1026", "queue");
    //Topic* topic = new Topic("#0.0.1027", "topic");
    MessageProducer* prod1 = sess1->createProducer(queue);
    MessageProducer* prod2 = sess2->createProducer(topic);
    MessageConsumer* cons1 = sess1->createConsumer(queue);
    MessageConsumer* cons2 = sess2->createConsumer(topic);

    Message* msg1 = sess1->createMessage();
    Message* msg2 = sess1->createMessage();
    Message* msg3 = sess1->createMessage();
    Message* msg4 = sess1->createMessage();
    Message* msg5 = sess1->createMessage();
 
    prod1->send(msg1);
    // printf("##### Message sent on queue: %s\n", msg1->getMessageID());

    prod1->send(msg2);
    // printf("##### Message sent on queue: %s\n", msg2->getMessageID());
    
    prod1->send(msg3);
    //printf("##### Message sent on queue: %s\n", msg3->getMessageID());
   
    prod1->send(msg4);
    //printf("##### Message sent on queue: %s\n", msg4->getMessageID());
 
    prod1->send(msg5);
    //printf("##### Message sent on queue: %s\n", msg5->getMessageID());

    Message* msg6 = sess2->createMessage();
    prod2->send(msg6);
    //printf("##### Message sent on topic: %s\n", msg6->getMessageID());

    Message* msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg1->getMessageID(), msg->getMessageID());

    msg = cons2->receive();
    // printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg6->getMessageID(), msg->getMessageID());
    
    msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg2->getMessageID(), msg->getMessageID());
    
    msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg3->getMessageID(), msg->getMessageID());

    msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg4->getMessageID(), msg->getMessageID());
    
    msg = cons1->receive();
    //printf("##### Message received: %s\n", msg->getMessageID());
    BaseTestCase::assertEquals( msg5->getMessageID(), msg->getMessageID());

    sess1->close();
    sess2->close();
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
