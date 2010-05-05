/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
package joram.dmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : Set 2 specifics DMQs, one for queue and another for user. 
 *        Set threshold either for queue and user, verify that messages are
 *        well forwarded to the right DMQ.
 *        Send anew the message and verify that the threshold mechanism works.
 *    
 */
public class TestDmq7 extends TestCase {
  public static void main(String[] args) {
    new TestDmq7().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      // open an administration connection 
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      // create a Queue   
      Queue queue =Queue.create("queue"); 
      Topic topic = Topic.create("topic"); 

      Queue dmq = Queue.create("dmq");

      // create a user
      User user = User.create("anonymous", "anonymous");

      // set permissions
      queue.setFreeReading();
      queue.setFreeWriting();
      topic.setFreeReading();
      topic.setFreeWriting();
      queue.setDMQId(dmq.getName());
      queue.setThreshold(2);
      user.setDMQId(dmq.getName());
      user.setThreshold(2);
      dmq.setFreeReading();
      dmq.setFreeWriting();

      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
      System.out.println("admin config ok");
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      // creates connection, session, messages producers and consumers needed
      // for tests.
      Connection cnx = cf.createConnection();
      Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session1.createProducer(null);
      Session session2 = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_queue = session2.createConsumer(queue);
      MessageConsumer cons_topic= session2.createConsumer(topic);
      cnx.start();
      
      // Creates connection, session and messages consumer needed to read messages
      // from dmq.
      Connection cnx_dmq = cf.createConnection();
      Session session_dmq = cnx_dmq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_dmq = session_dmq.createConsumer(dmq);
      cnx_dmq.start();

      // Part 1 - Test the Queue.
      
      // create a text message and send it to the queue
      TextMessage msg1 = session1.createTextMessage("test to queue");
      producer.send(queue, msg1);
      System.out.println("msg sent: " + msg1.getJMSMessageID());
      
      // read the message and rollback it x2
      TextMessage msg2 = (TextMessage) cons_queue.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #1: " + msg2.getJMSMessageID());
      session2.rollback();
      msg2 = (TextMessage) cons_queue.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #2: " + msg2.getJMSMessageID());
      session2.rollback();

      // read the message on the DMQ
      msg2 = (TextMessage) cons_dmq.receive();
      System.out.println("msg recv on dmq: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      
      // send anew the message on the queue
      msg1 = msg2;
      producer.send(queue, msg1);
      System.out.println("msg sent #2: " + msg1.getJMSMessageID() + " -> " + msg1.getIntProperty("JMSXDeliveryCount"));
      
      // read the message and rollback it x2
      msg2 = (TextMessage) cons_queue.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #3: " + msg2.getJMSMessageID());
      session2.rollback();
      msg2 = (TextMessage) cons_queue.receive(); 
      System.out.println("msg recv and rollback #4: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      session2.rollback();
      
      // Then verify that it is correctly sent to the dmq
      msg2 = (TextMessage) cons_dmq.receive();
      System.out.println("msg recv on dmq #2: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      
      // Part 2 - Test the Topic.

      // create a text message and send it to the queue
      msg1 = session1.createTextMessage("test to topic");
      producer.send(topic, msg1);
      System.out.println("msg sent: " + msg1.getJMSMessageID());
      
      // read the message and rollback it x2
      msg2 = (TextMessage) cons_topic.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #1: " + msg2.getJMSMessageID());
      session2.rollback();
      msg2 = (TextMessage) cons_topic.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #2: " + msg2.getJMSMessageID());
      session2.rollback();

      // read the message on the DMQ
      msg2 = (TextMessage) cons_dmq.receive();
      System.out.println("msg recv on dmq: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      
      // send anew the message on the queue
      msg1 = msg2;
      producer.send(topic, msg1);
      System.out.println("msg sent #2: " + msg1.getJMSMessageID() + " -> " + msg1.getIntProperty("JMSXDeliveryCount"));
      
      // read the message and rollback it x2
      msg2 = (TextMessage) cons_topic.receive(); 
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      System.out.println("msg recv and rollback #3: " + msg2.getJMSMessageID());
      session2.rollback();
      msg2 = (TextMessage) cons_topic.receive(); 
      System.out.println("msg recv and rollback #4: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
      session2.rollback();
      
      // Then verify that it is correctly sent to the dmq
      msg2 = (TextMessage) cons_dmq.receive();
      System.out.println("msg recv on dmq #2: " + msg2.getJMSMessageID());
      assertTrue("bad message id", (msg1.getJMSMessageID().equals(msg2.getJMSMessageID())));
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}

