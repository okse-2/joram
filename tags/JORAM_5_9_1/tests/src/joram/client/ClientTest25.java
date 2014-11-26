/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s):
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * When closing a session the messages delivered but not consumed by the client must not be set to redelivered and JMSXDeliveryCount = 1.
 * 
 * Test: first with a queue and second with a topic.
 * - producer send 5 messages on destination.
 * - a MessageConsumer (listener mode) receive the message (in queue mode set QueueMessageReadMax=5).
 * - on first message received we close the consumer session.
 * - create a new session (CLIENT_ACKNOWLEDGE)
 * - received messages and verify the redelivered value expected false and JMSXDeliveryCount = 1.
 * - close the session and forgot the acknowledge.
 * - create a new session
 * - received messages and verify the redelivered value expected true and JMSXDeliveryCount = 2.
 * 
 * @see JORAM-41.
 */
public class ClientTest25 extends TestCase {

  public static void main(String[] args) {
    new ClientTest25().run();
  }

  public static Object lock = new Object();
  public static Session recSession;

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
          org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create(0, "test_queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      Topic topic = Topic.create(0, "test_topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      ConnectionFactory cf = 
          org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

      Connection connection = cf.createConnection("anonymous", "anonymous");
      connection.setClientID("ClientTest25");
      connection.start();

      testRedelivered(queue, connection);

      testRedelivered(topic, connection);

      Thread.sleep(1000);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }

  public static void testRedelivered(final Destination dest, Connection connection) throws Exception {
    System.out.println("\ntest the " + dest.getClass().getSimpleName());
    
    recSession = (Session) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    recSession.setQueueMessageReadMax(5);
    
    MessageConsumer consumer = null;
    
    if (dest instanceof Queue)
      consumer = recSession.createConsumer(dest);
    else
      consumer = recSession.createDurableSubscriber((Topic) dest, "dursub");

    // send message
    Session sendSession = (Session) connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sendSession.createProducer(dest);
    for (int i = 0; i < 5; i++) {
      TextMessage m = sendSession.createTextMessage("test" + i);
      producer.send(m);
    }
    sendSession.commit();
    
    consumer.setMessageListener(new MessageListener() {
      int i = 0;
      public void onMessage(Message msg) {
        try {
          System.out.println("on message " + ((TextMessage)msg).getText() + ", isRedelivered = " + msg.getJMSRedelivered() + ", JMSXDeliveryCount = " + msg.getObjectProperty("JMSXDeliveryCount"));
          if (i == 0) {
            assertEquals("the JMSRedelivered is bad.", false, msg.getJMSRedelivered());
            assertEquals("Bad value for JMSXDeliveryCount.", 1, msg.getObjectProperty("JMSXDeliveryCount"));
          }
          i++;
          Thread.sleep(1000);
          synchronized (lock) {
            lock.notify();
          }
        } catch(Exception exc) {
          exc.printStackTrace();
        }
      }
    });

    synchronized (lock) {
      lock.wait(60000);
    }
    recSession.close();

    
    System.out.println("new session CLIENT_ACKNOWLEDGE ...");
    
    recSession = (Session) connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    recSession.setQueueMessageReadMax(5);
    
    if (dest instanceof Queue)
      consumer = recSession.createConsumer(dest);
    else
      consumer = recSession.createDurableSubscriber((Topic) dest, "dursub");
    
    consumer.setMessageListener(new MessageListener() {
      int i= 0;
      public void onMessage(Message msg) {
        try {
          System.out.println("on message " + ((TextMessage)msg).getText() + ", isRedelivered = " + msg.getJMSRedelivered() + ", JMSXDeliveryCount = " + msg.getObjectProperty("JMSXDeliveryCount"));
          if (i == 0) {
            assertEquals("the JMSRedelivered is bad.", false, msg.getJMSRedelivered());
            assertEquals("Bad value for JMSXDeliveryCount.", 1, msg.getObjectProperty("JMSXDeliveryCount"));
          }
          i++;
          Thread.sleep(1000);
          synchronized (lock) {
            lock.notify();
          }
        } catch(Exception exc) {}
      }
    });

    
    synchronized (lock) {
      lock.wait(60000);
    }
    recSession.close();
    
    System.out.println("new session AUTO_ACKNOWLEDGE ...");
    
    recSession = (Session) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    recSession.setQueueMessageReadMax(5);
    if (dest instanceof Queue)
      consumer = recSession.createConsumer(dest);
    else
      consumer = recSession.createDurableSubscriber((Topic) dest, "dursub");
    consumer.setMessageListener(new MessageListener() {
      int i = 0;
      public void onMessage(Message msg) {
        try {
          System.out.println("on message " + ((TextMessage)msg).getText() + ", isRedelivered = " + msg.getJMSRedelivered() + ", JMSXDeliveryCount = " + msg.getObjectProperty("JMSXDeliveryCount"));
          if (i == 0) {
            assertEquals("the JMSRedelivered is bad.", true, msg.getJMSRedelivered());
            assertEquals("Bad value for JMSXDeliveryCount.", 2, msg.getObjectProperty("JMSXDeliveryCount"));
          }
          i++;
        } catch(JMSException exc) {}
      }
    });
    
    Thread.sleep(2000);
  }
}
