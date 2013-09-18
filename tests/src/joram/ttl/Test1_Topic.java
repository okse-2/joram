/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.ttl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;


/**
 * Test ttl on a distributed architecture. Use 2 servers. The producer is
 * attached to server 0. The consumer is attached to server 1. The topic is
 * created on server 0.
 * 
 */
public class Test1_Topic extends TestCase {

  public static void main(String[] args) {
    new Test1_Topic().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short) 0);
      startAgentServer((short) 1);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Topic topic = (Topic) ictx.lookup("topic");
      DeadMQueue dmqueue0 = (DeadMQueue) ictx.lookup("dmqueue0");
      DeadMQueue dmqueue1 = (DeadMQueue) ictx.lookup("dmqueue1");
      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
      ictx.close();

      Connection cnx = cf0.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sessionp.createProducer(topic);
      cnx.start();
      
      Connection cnxCons = cf1.createConnection();
      cnxCons.setClientID("Test1_Topic");
      Session sessionc = cnxCons.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber consumer = sessionc.createDurableSubscriber(topic, "top");
      cnxCons.start();

      TextMessage msg = null;
      for (int j = 0; j < 3; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 2000);
      }

      // Waiting for the messages to be out of date
      Thread.sleep(4000);
      TextMessage msg1 = (TextMessage) consumer.receive(1000);
      assertEquals(null, msg1);

      cnxCons.stop();
      
      // Messages should be present on the DMQ
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(3, dmqueue1.getPendingMessages());
      AdminModule.disconnect();

      // the server containing the queue is stopped
      stopAgentServer((short) 1);
      Thread.sleep(2000);

      for (int j = 0; j < 10; j++) {
        msg = sessionp.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 1000);
      }

      // Waiting for the messages to be out of date
      Thread.sleep(10000);
      startAgentServer((short) 1);
      Thread.sleep(2000);

      // No additional messages should be present on the DMQ1, they should have
      // been sent to DMQ0 before traveling on the network
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(10, dmqueue0.getPendingMessages());
      assertEquals(3, dmqueue1.getPendingMessages());
      AdminModule.disconnect();

      cnx.close();
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
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    System.out.println("Admin");
    // connection
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Topic
    org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic.create(0);
    // create a DMQueue
    DeadMQueue dmqueue0 = (DeadMQueue) DeadMQueue.create(0);
    DeadMQueue dmqueue1 = (DeadMQueue) DeadMQueue.create(1);
    AdminModule.setDefaultDMQ(0, dmqueue0);

    // create a user
    User.create("anonymous", "anonymous", 0);
    User user = User.create("anonymous", "anonymous", 1);
    user.setDMQ(dmqueue1);
    // set permissions
    topic.setFreeReading();
    topic.setFreeWriting();

    ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 2560);
    ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 2561);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("dmqueue0", dmqueue0);
    jndiCtx.bind("dmqueue1", dmqueue1);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
