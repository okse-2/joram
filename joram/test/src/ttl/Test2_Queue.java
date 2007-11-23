/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package ttl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test ttl on a distributed architecture. Use 2 servers. The producer is
 * attached to server 0. The consumer is attached to server 1. The queue is
 * created on server 1.
 * 
 */
public class Test2_Queue extends TestCase {

  public static void main(String[] args) {
    new Test2_Queue().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short) 0);
      startAgentServer((short) 1);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      DeadMQueue dmqueue = (DeadMQueue) ictx.lookup("dmqueue");
      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
      ictx.close();

      Connection cnx = cf0.createConnection();
      Connection cnxCons = cf1.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnxCons.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();
      cnxCons.start();

      MessageConsumer consumer = sessionc.createConsumer(queue);
      MessageProducer producer = sessionp.createProducer(queue);

      TextMessage msg = null;
      TextMessage msg1 = null;

      for (int j = 0; j < 10; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 3000);
      }
      
      // Waiting for the messages to be out of date
      Thread.sleep(6000);

      msg1 = (TextMessage) consumer.receive(1000);
      assertEquals(null, msg1);
      
      // Messages should be present on the DMQ
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(10, dmqueue.getPendingMessages());
      AdminModule.disconnect();

      // the server containing the queue is stopped
      stopAgentServer((short) 1);
      
      for (int j = 0; j < 10; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 3000);
      }

      // Waiting for the messages to be out of date
      Thread.sleep(6000);

      startAgentServer((short) 1);

      // No additional messages should be present on the DMQ, they should have
      // been deleted by the network
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(10, dmqueue.getPendingMessages());
      AdminModule.disconnect();
      
      cnx.close();
      cnxCons.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Servers stop ");
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      endTest();
    }
  }

  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(1);
    // create a DMQueue
    DeadMQueue dmqueue = (DeadMQueue) DeadMQueue.create(1);

    // create a user
    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);
    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 1);
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();
    queue.setDMQ(dmqueue);
    
    ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 2560);
    ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 2561);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("dmqueue", dmqueue);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
