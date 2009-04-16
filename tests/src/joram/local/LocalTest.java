/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.local;


import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;

import framework.TestCase;

/**
 * Test local : try to send a message and receive it with a queue and with a topic
 */
public class LocalTest extends TestCase {
  public LocalTest() {
    super();
  }

  public void run() {
    try {
      System.getProperties().put("Transaction", "fr.dyade.aaa.util.NullTransaction");
      fr.dyade.aaa.agent.AgentServer.init(new String[]{"0", "s0"});
      fr.dyade.aaa.agent.AgentServer.start();

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf = LocalConnectionFactory.create();
      QueueConnectionFactory qcf = QueueLocalConnectionFactory.create();
      TopicConnectionFactory tcf = TopicLocalConnectionFactory.create();

      Topic topic = Topic.create(0);
      topic.setFreeReading();
      topic.setFreeWriting();

      Queue queue = Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();
      
      try {
        // Test1 - Queue and Topic unified
        Connection cnx = cf.createConnection();
        Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = sess1.createProducer(null);
        
        Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer qconsumer = sess2.createConsumer(queue);
        MessageConsumer tconsumer = sess2.createConsumer(topic);
       
        cnx.start();

        TextMessage msg1 = sess1.createTextMessage();
        msg1.setText("Test Queue with LocalConnectionFactory");
        producer.send(queue, msg1);

        TextMessage msg2 = (TextMessage) qconsumer.receive();
 
        assertTrue(msg1.getText().equals(msg2.getText()));
        
        msg1 = sess1.createTextMessage();
        msg1.setText("Test Topic LocalConnectionFactory");
        producer.send(topic, msg1);

        msg2 = (TextMessage) tconsumer.receive();
 
        assertTrue(msg1.getText().equals(msg2.getText()));

        cnx.close();
      } catch (Exception exc) {
        throw exc;
      }

      try {
        // Test2 - Queue sender-receiver
        QueueConnection cnx = qcf.createQueueConnection();
        QueueSession sess1 = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueSender sender = sess1.createSender(queue);

        QueueSession sess2 = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueReceiver receiver = sess2.createReceiver(queue);    
        
        cnx.start();

        // Send a message
        TextMessage msg1 = sess1.createTextMessage();
        msg1.setText("Test QueueLocalConnectionFactory");
        sender.send(msg1);
        
        TextMessage msg2 = (TextMessage) receiver.receive();
 
        assertTrue(msg1.getText().equals(msg2.getText()));
        
        cnx.close();
      } catch (Exception exc) {
        throw exc;
      }
      
      try {
        // Test3 - Topic pub/sub
        TopicConnection cnx = tcf.createTopicConnection();
        TopicSession sess1 = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher publisher = sess1.createPublisher(topic);
        
        TopicSession sess2 = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicSubscriber subscriber = sess2.createSubscriber(topic);
        
        cnx.start();

        TextMessage msg1 = sess1.createTextMessage();
        msg1.setText("Test QueueLocalConnectionFactory");
        publisher.send(msg1);

        TextMessage msg2 = (TextMessage) subscriber.receive();
 
        assertTrue(msg1.getText().equals(msg2.getText()));
        
        cnx.close();
      } catch (Exception exc) {
        throw exc;
      }
    } catch (Exception exc) {
      error(exc);
    } finally {
      // stopAgentServer((short)0);
      fr.dyade.aaa.agent.AgentServer.stop();
      endTest();     
    }
  }

  public static void main(String args[]) {
    new LocalTest().run();
  }
}
