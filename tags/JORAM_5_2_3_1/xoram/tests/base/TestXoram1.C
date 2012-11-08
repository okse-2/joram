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

int main (int argc, char *argv[]) {
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
    BaseTestCase::error(&exc);
  } catch (...) {
    printf("##### exception\n");
    BaseTestCase::error(new Exception(" catch ..., unknown exception "));
  }
  printf("##### bye\n");
  BaseTestCase::endTest();
}
