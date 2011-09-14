/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2011 ScalAgent Distributed Technologies
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
package joram.admin;

import java.util.Hashtable;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *  Verify the behavior of multiples administration operation, first locally
 * then remotely: <ul>
 * <li>User creation and configuration, setting DMQ and threshold.</li>
 * <li>Queue and topic creation and configuration, setiing right, DMQ and Threshold.</li>
 * <li>Quick verification of behavior of DMQ, threshold and maximum number of messages.</li>
 * </ul>
 */
public class AdminTest8 extends TestCase {

  public static void main(String[] args) {
    new AdminTest8().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      
      Thread.sleep(2000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
      
      adminAndTest(cf);
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
  
  public void adminAndTest(ConnectionFactory cf) throws Exception {
    AdminModule.connect(cf, "root", "root");

    // Create a dead message queue
    Queue dmq1 = Queue.create("dmq1");
    dmq1.setFreeReading();
    dmq1.setFreeWriting();
    
    // Create another dead message queue
    Queue dmq2 = Queue.create("dmq2");
    dmq2.setFreeReading();
    dmq2.setFreeWriting();

    // Set default DMQ and threshold
    AdminModule.setDefaultDMQ(dmq1);

    // Create the anonymous user and configure it
    User user = User.create("anonymous", "anonymous");
    user.setDMQ(dmq2);
    user.setThreshold(5);      

    // Create a topic and configure it
    Topic topic = Topic.create("topic");
    topic.setFreeReading();
    topic.setFreeWriting();
    topic.setDMQ(dmq1);

    // Verify the user 
    User userX = User.create("anonymous", "anonymous");

    assertTrue("Bad user", user.equals(userX));
    assertTrue("Bad DMQ for user", userX.getDMQ().equals(dmq2));
    assertTrue("Bad threshold for user", (userX.getThreshold() == 5));
    
    // Verify that there is no existing session
    Subscription[] subs = user.getSubscriptions();
    assertTrue("Bad number of subscriptions:" + subs.length, (subs.length == 0));

    Connection connection = cf.createConnection("anonymous", "anonymous");
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    MessageProducer producer = session.createProducer(topic);
    MessageConsumer consumer1 = session.createConsumer(dmq1);      
    MessageConsumer consumer2 = session.createConsumer(dmq2);

    Session session2 = connection.createSession(true, Session.SESSION_TRANSACTED);
    MessageConsumer consumer3 = session2.createConsumer(topic);

    connection.start();

    // Verify that the corresponding session exists
    subs = user.getSubscriptions();
    assertTrue("Bad number of subscriptions:" + subs.length, (subs.length == 1));
    
    String subname = null;
    if (subs.length == 1) {
      // Verify the default value of the subscription
      assertTrue("Bad topic identification for subscription", subs[0].getTopicId().equals(topic.getName()));
      assertFalse("Subscription shouldn't be durable", subs[0].isDurable());
      subname = subs[0].getName();
      assertTrue("Bad number of messages in subscription", (subs[0].getMessageCount() == 0));
      
      // Configure the subscription
      user.setNbMaxMsg(subname, 2);
      user.setThreshold(subname, 2);
      
      // Verify the user and subscription configurations
      assertTrue("Bad threshold for user", (user.getThreshold() == 5));
      assertTrue("Bad value for NbMaxMsg of subscription", (user.getNbMaxMsg(subname) == 2));
      assertTrue("Bad threshold for subscription", (user.getThreshold(subname) == 2));
    }
    
    // Send a 1st message on topic 
    Message msg1 = session.createMessage();
    producer.send(msg1);
    // Send a 2nd message on topic that will be redirected to dmq2 (threshold)

    Message msg2 = session.createMessage();
    producer.send(msg2);
    // Send a 3rd message that will be redirected to dmq2 (NbMaxMsg)
    Message msg3 = session.createMessage();
    producer.send(msg3);

    // Verify that the right message is available on dmq2
    Message msg = consumer2.receive(1000L);
    assertTrue("No message on dmq2", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq2", msg.getJMSMessageID().equals(msg3.getJMSMessageID()));

    if (subname != null) {
      Subscription sub = user.getSubscription(subname);
      assertTrue("Bad number of messages in subscription", (sub.getMessageCount() == 2));
    }
    
    msg = consumer3.receive(1000L);
    assertTrue("No message on subscription", (msg != null));
    if (msg != null)
      assertTrue("Bad message on subscription", msg.getJMSMessageID().equals(msg1.getJMSMessageID()));
    session2.commit();

    msg = consumer3.receive(1000L);
    assertTrue("No message on subscription", (msg != null));
    if (msg != null)
      assertTrue("Bad message on subscription", msg.getJMSMessageID().equals(msg2.getJMSMessageID()));
    session2.rollback();
    
    msg = consumer3.receive(1000L);
    assertTrue("No message on subscription", (msg != null));
    if (msg != null)
      assertTrue("Bad message on subscription", msg.getJMSMessageID().equals(msg2.getJMSMessageID()));
    session2.rollback();

    // Verify that the right message is available on dmq2
    msg = consumer2.receive(1000L);
    assertTrue("No message on dmq2", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq2", msg.getJMSMessageID().equals(msg2.getJMSMessageID()));
    
    connection.close();
    AdminModule.disconnect();

  }
}
