/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.tcp;


import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * One agent server #0.
 * Two destinations: 1 queue and 1 topic.
 * 
 * Test1:
 * Send MESSAGE_NUMBER messages to a queue in
 * transacted mode. Commit. Check the message
 * number with a queue browser.
 * Receive MESSAGE_NUMBER messages. Commit.
 * Check again the queue size.
 * 
 * Test2:
 * Create a topic subscriber.
 * Send MESSAGE_NUMBER messages to the topic
 * in transacted mode. Commit.
 * Receive MESSAGE_NUMBER messages (explicitely). Commit.
 * Close the message prod/cons, session and connection.
 * 
 * @author feliot
 *
 *
 */
public class Test extends TestCase {

  public static final int MESSAGE_NUMBER = 10;

  public Test() {
    super();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      TopicConnectionFactory tcf = 
        org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory.create("localhost", 2560);
      // tcf.getParameters().cnxPendingTimer = 5000;
      
      QueueConnectionFactory qcf =
        org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory.create("localhost", 2560);

      org.objectweb.joram.client.jms.Topic topic = 
        org.objectweb.joram.client.jms.Topic.create(0);
      topic.setFreeReading();
      topic.setFreeWriting();

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      // Test1 - Queue sender-receiver
      QueueConnection qc = qcf.createQueueConnection();
      QueueSession qs = qc.createQueueSession(true, 0);
      QueueSender qsend = qs.createSender(queue);
      QueueReceiver qrec = qs.createReceiver(queue);
      TextMessage msg = qs.createTextMessage();
      QueueBrowser qb = qs.createBrowser(queue);
      qc.start();

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        msg.setText("Test number " + i);
        qsend.send(msg);
      }      
      qs.commit();
      
      if (checkQueue(qb, MESSAGE_NUMBER) == MESSAGE_NUMBER) {
        for (int i = 0; i < MESSAGE_NUMBER; i++) {
          msg = (TextMessage)qrec.receive();
          //System.out.println("Msg received: " + msg.getText());
	  assertTrue(msg.getText().startsWith("Test number "));
        }
        qs.commit();
      }
      
      checkQueue(qb, 0);
      
      qb.close();
      qsend.close();
      qrec.close();
      qs.close();
      qc.close();

      // Test2 - Topic pub/sub
      TopicConnection tc = tcf.createTopicConnection();
      TopicSession ts = tc.createTopicSession(true, 0);
      TopicPublisher tpub = ts.createPublisher(topic);
      TopicSubscriber tsub = ts.createSubscriber(topic);
      msg = ts.createTextMessage();
      tc.start();
      
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        msg.setText("Test number " + i);
        tpub.publish(msg);
      }
      ts.commit();

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        msg = (TextMessage)tsub.receive();
	// System.out.println("Msg received: " + msg.getText());
 assertTrue(msg.getText().startsWith("Test number "));
      }
      ts.commit();
      
      
      tpub.close();
      tsub.close();
      ts.close();
      tc.close();
      
      AdminModule.disconnect();
    } catch (Exception exc) {
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }

  public int checkQueue(QueueBrowser qb,
                        int expectedMessageNumber) 
    throws JMSException {
    Enumeration messages = qb.getEnumeration();
    int counter = 0;
    while (messages.hasMoreElements()) {
      counter++;
      messages.nextElement();
    }
    assertTrue(
      "Queue: " + counter + 
      " messages in queue instead of " + expectedMessageNumber,
      counter == expectedMessageNumber);
    return counter;
  }

  public static void main(String args[]) {
    new Test().run();
  }
}
