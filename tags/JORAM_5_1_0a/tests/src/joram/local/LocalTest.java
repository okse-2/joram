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
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.local;


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

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * Test local : try to send a message and receive it with a queue and with a topic
 *
 */
public class LocalTest extends TestCase {

  public static final String TRANSACTION = "Transaction";
  public static final String NULL_TRANSACTION = 
      "fr.dyade.aaa.util.NullTransaction";
  public static final int MESSAGE_NUMBER = 10;

  public LocalTest() {
    super();
  }

  public void run() {
    try {
      System.getProperties().put(TRANSACTION, NULL_TRANSACTION);
      fr.dyade.aaa.agent.AgentServer.init(new String[]{"0", "s0"});
      fr.dyade.aaa.agent.AgentServer.start();

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      TopicConnectionFactory tcf = 
        org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory.create();
      
      QueueConnectionFactory qcf =
        org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory.create();

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
      final QueueSession sendQs = qc.createQueueSession(true, 0);
      final QueueSender qsend = sendQs.createSender(queue);

      QueueSession recQs = qc.createQueueSession(true, 0);
      QueueReceiver qrec = recQs.createReceiver(queue);      
      QueueBrowser qb = recQs.createBrowser(queue);
      qc.start();

      new Thread(new Runnable() {
          public void run() {
	      //System.out.println("Producer");
            try {
              for (int i = 0; i < MESSAGE_NUMBER; i++) {
                TextMessage msg = sendQs.createTextMessage();
                msg.setText("Test number " + i);
                //System.out.println("Test number " + i);
                qsend.send(msg);
              }
              //System.out.println("Commit");
              sendQs.commit();
            } catch (Exception exc) {
              exc.printStackTrace();
            }
          }
        }).start();

      
      //if (checkQueue(qb, MESSAGE_NUMBER) == MESSAGE_NUMBER) {
      
      //System.out.println("Consumer");
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = (TextMessage)qrec.receive();
	// System.out.println("Msg received: " + msg.getText());
	assertTrue( msg.getText().startsWith("Test number "));
      }
      recQs.commit();
    
      checkQueue(qb, 0);

      // Test2 - Topic pub/sub
      TopicConnection tc = tcf.createTopicConnection();
      TopicSession ts = tc.createTopicSession(true, 0);
      TopicPublisher tpub = ts.createPublisher(topic);
      TopicSubscriber tsub = ts.createSubscriber(topic);
      tc.start();
      
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = ts.createTextMessage();
        msg.setText("Test number " + i);
        tpub.publish(msg);
      }
      ts.commit();

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = (TextMessage)tsub.receive();
	//        System.out.println("Msg received: " + msg.getText());
	assertTrue( msg.getText().startsWith("Test number "));
      }
      ts.commit();
      
      qc.close();
      tc.close();
      AdminModule.disconnect();
    } catch (Exception exc) {
      error(exc);
    } finally {
      // stopAgentServer((short)0);
      fr.dyade.aaa.agent.AgentServer.stop();
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
    new LocalTest().run();
  }
}
