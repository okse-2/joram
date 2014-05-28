/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

import framework.TestCase;

/**
 * Test the occurrence of various exceptions:
 * <ul>
 * <li>Access a queue with a topic session.</li>
 * <li>Send a message without having writing right.</li>
 * <li>Commit a non-transacted session.</li>
 * <li>Receive the message without having reading right.</li>
 * <li>Change reading right while a consumer is waiting a message.</li>
 * <li>Delete a queue while a consumer is waiting a message.</li>
 * <li>Recreate an existing subscription.</li>
 * <li>Delete a non existing subscription.</li>
 * </ul>
 */
public class Test_Exceptions extends TestCase {

  public static void main(String[] args) {
    new Test_Exceptions().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      Topic topic = (Topic) ictx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
      ictx.close();
      
      TopicConnection tcnx = tcf.createTopicConnection();
      Session topicSession = tcnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      tcnx.start();

      try {
        // Access a queue with a topic session
        topicSession.createBrowser(queue);
        assertTrue(false);
      } catch (IllegalStateException exc) {
        // OK
      }
      tcnx.close();

      Connection cnx = cf.createConnection();
      cnx.setClientID("Test_Exceptions");
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      MessageProducer producer = sessionp.createProducer(queue);
      final MessageConsumer consumer = sessionc.createConsumer(queue);

      Message msg = sessionp.createMessage();
      try {
        // Send the message without having writing right.
        producer.send(msg);
        assertTrue(false);
      } catch (JMSSecurityException exc) {
        // OK
      }

      AdminModule.connect(cf);
      queue.setFreeWriting();
      AdminModule.disconnect();

      producer.send(msg);
      
      try {
        // Commit a non-transacted session
        sessionp.commit();
        assertTrue(false);
      } catch (IllegalStateException exc) {
        // OK
      }

      try {
        // Receive the message without having reading right.
        consumer.receive();
        assertTrue(false);
      } catch (JMSSecurityException exc) {
        // OK
      }

      AdminModule.connect(cf);
      queue.setFreeReading();
      AdminModule.disconnect();

      Message msg1 = consumer.receive(1000);
      assertNotNull(msg1);

      new Thread(new Runnable() {
        public void run() {
          try {
            consumer.receive(5000);
            assertTrue(false);
          } catch (JMSSecurityException exc) {
            // OK
          } catch (JMSException exc) {
            assertTrue(false);
          }
        }
      }).start();

      Thread.sleep(1000);

      // Change the rights while a consumer is waiting a message
      AdminModule.connect(cf);
      queue.unsetFreeReading();
      AdminModule.disconnect();

      Thread.sleep(1000);

      AdminModule.connect(cf);
      queue.setFreeReading();
      AdminModule.disconnect();

      new Thread(new Runnable() {
        public void run() {
          try {
            consumer.receive(5000);
            assertTrue(false);
          } catch (InvalidDestinationException exc) {
            // OK
          } catch (JMSException exc) {
            assertTrue(false);
          }
        }
      }).start();

      Thread.sleep(1000);

      // Delete a queue while a consumer is waiting a message
      AdminModule.connect(cf);
      queue.delete();
      AdminModule.disconnect();

      TopicSubscriber subscriber = sessionc.createDurableSubscriber(topic, "sub");
      // Recreate an existing subscription
      try {
        sessionc.createDurableSubscriber(topic, "sub");
        assertTrue(false);
      } catch (JMSException exc) {
        // OK
      }

      subscriber.close();

      sessionc.unsubscribe("sub");
      // Delete a non existing subscription
      try {
        sessionc.unsubscribe("sub");
        assertTrue(false);
      } catch (JMSException exc) {
        // OK
      }

      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  /**
   * Admin : Create queue and a user anonymous
   * use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);

    // create a Queue
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create("queue");
    org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic.create("topic");

    topic.setFreeReading();
    topic.setFreeWriting();

    // create a user
    User.create("anonymous", "anonymous");

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    TopicConnectionFactory tcf = TopicTcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("tcf", tcf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
