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
package joram.ttl;

import java.util.Properties;

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

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


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
      DeadMQueue dmqueue1 = (DeadMQueue) ictx.lookup("dmqueue1");
      DeadMQueue dmqueue0 = (DeadMQueue) ictx.lookup("dmqueue0");
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
      MessageConsumer consumerdq1 = sessionc.createConsumer(dmqueue1);
      MessageProducer producer = sessionp.createProducer(queue);

      TextMessage msg = null;

      for (int j = 0; j < 3; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 2000);
      }
      
      // Waiting for the messages to be out of date
      Thread.sleep(4000);

      msg = (TextMessage) consumer.receive(1000);
      assertEquals(null, msg);
      
      // Messages should be present on the DMQ
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      assertEquals(3, dmqueue1.getPendingMessages());
      AdminModule.disconnect();

      // Check some message properties
      msg = (TextMessage) consumerdq1.receive(500);
      assertTrue(msg.getBooleanProperty("JMS_JORAM_EXPIRED"));
      System.out.println("Expired at: " + msg.getLongProperty("JMS_JORAM_EXPIRATIONDATE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_NOTWRITABLE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_UNDELIVERABLE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_ADMINDELETED"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_DELETEDDEST"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_QUEUEFULL"));

      // the server containing the queue is stopped
      stopAgentServer((short) 1);
      Thread.sleep(1000);
      
      for (int j = 0; j < 10; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 2000);
      }

      // Waiting for the messages to be out of date
      Thread.sleep(4000);

      startAgentServer((short) 1);

      // No additional messages should be present on the DMQ1, they should have
      // been sent to DMQ0 before traveling on the network
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(2, dmqueue1.getPendingMessages());
      assertEquals(10, dmqueue0.getPendingMessages());
      assertEquals(0, ((org.objectweb.joram.client.jms.Queue) queue).getPendingMessages());
      AdminModule.disconnect();
      
      // Check some message properties
      MessageConsumer consumerdq0 = sessionp.createConsumer(dmqueue0);
      msg = (TextMessage) consumerdq0.receive(500);
      assertTrue(msg.getBooleanProperty("JMS_JORAM_EXPIRED"));
      System.out.println("Expired at: " + msg.getLongProperty("JMS_JORAM_EXPIRATIONDATE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_NOTWRITABLE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_UNDELIVERABLE"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_ADMINDELETED"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_DELETEDDEST"));
      assertFalse(msg.getBooleanProperty("JMS_JORAM_QUEUEFULL"));
      
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

    Properties prop = new Properties();
    prop.setProperty("period", "1000");

    // connection
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(1, prop);
    // create a DMQueue
    DeadMQueue dmqueue0 = (DeadMQueue) DeadMQueue.create(0);
    DeadMQueue dmqueue1 = (DeadMQueue) DeadMQueue.create(1);
    AdminModule.setDefaultDMQ(0, dmqueue0);

    // create a user
    User.create("anonymous", "anonymous", 0);
    User.create("anonymous", "anonymous", 1);
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();
    queue.setDMQ(dmqueue1);
    dmqueue0.setFreeReading();
    dmqueue1.setFreeReading();
    
    ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 2560);
    ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 2561);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("dmqueue0", dmqueue0);
    jndiCtx.bind("dmqueue1", dmqueue1);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
