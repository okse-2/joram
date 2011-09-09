/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2010 ScalAgent Distributed Technologies
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
public class AdminTest2 extends TestCase {

  public static void main(String[] args) {
    new AdminTest2().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      
      Thread.sleep(2000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
      
      adminAndTest(0, cf);
      adminAndTest(1, cf);
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 1);
      stopAgentServer((short) 0);
      endTest();     
    }
  }
  
  public void adminAndTest(int sid, ConnectionFactory cf) throws Exception {
    AdminModule.connect(cf, "root", "root");

    // Create a dead message queue
    Queue dmq1 = Queue.create(sid, "dmq1");
    dmq1.setFreeReading();
    dmq1.setFreeWriting();
    
    // Create another dead message queue
    Queue dmq2 = Queue.create(sid, "dmq2");
    dmq2.setFreeReading();
    dmq2.setFreeWriting();

    // Set default DMQ and threshold
    AdminModule.setDefaultDMQ(sid, dmq1);
    AdminModule.setDefaultThreshold(sid, 1);

    // Create the anonymous user needed for test
    User.create("anonymous", "anonymous");
    
    // Create a user and configure it
    User user = User.create("XXX", "XXX", sid);
    user.setDMQ(dmq2);
    user.setThreshold(3);      

    // Create a queue and configure it
    Queue queue1 = Queue.create(sid, "queue1");
    queue1.setFreeReading();
    queue1.setFreeWriting();
    queue1.setReader(user);
    queue1.setWriter(user);
    queue1.setDMQ(dmq2);
    queue1.setThreshold(3);
    
    // Create another queue and configure it
    Queue queue2 = Queue.create(sid, "queue2");
    queue2.setFreeReading();
    queue2.setFreeWriting();
    queue2.setNbMaxMsg(2);

    // Create a topic and configure it
    Topic topic = Topic.create(sid, "topic");
    topic.setFreeReading();
    topic.setFreeWriting();
    topic.setReader(user);
    topic.setWriter(user);
    topic.setDMQ(dmq2);

    // Verify the default configuration
    assertTrue("Bad default DMQ", AdminModule.getDefaultDMQ(sid).equals(dmq1));
    assertTrue("Bad default threshold", (AdminModule.getDefaultThreshold(sid) == 1));
    
    // Verify the user configuration
    User userX = User.create("XXX", "XXX", sid);

    assertTrue("Bad user", user.equals(userX));
    assertTrue("Bad DMQ for user", userX.getDMQ().equals(dmq2));
    assertTrue("Bad threshold for user", (userX.getThreshold() == 3));
    
    // Verify the first queue configuration
    Queue queueX = Queue.create(sid, "queue1");
    assertTrue("Bad queue", queue1.equals(queueX));
    List readers = queueX.getReaders();
    assertTrue("Bad readers list size for queue", (readers.size() == 1));
    assertTrue("Bad readers list for queue", readers.contains(user));
    List writers = queueX.getWriters();
    assertTrue("Bad writers list size for queue", (writers.size() == 1));
    assertTrue("Bad writers list for queue", writers.contains(user));
    assertTrue("Bad DMQ for queue", queueX.getDMQ().equals(dmq2));
    assertTrue("Bad threshold for queue", (queueX.getThreshold() == 3));
    
    Hashtable h = queueX.getStatistics();
    assertTrue("Bad value for Name attribute", h.get("Name").equals(queue1.getAdminName()));
    assertTrue("CreationDate attribute missing", h.containsKey("CreationDate"));
    assertTrue("Bad value for NbMsgsDeliverSinceCreation attribute",
               h.get("NbMsgsDeliverSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsReceiveSinceCreation attribute",
               h.get("NbMsgsReceiveSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsSentToDMQSinceCreation attribute",
               h.get("NbMsgsSentToDMQSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for WaitingRequestCount attribute",
               h.get("WaitingRequestCount").equals(new Integer(0)));
    assertTrue("Bad value for DeliveredMessageCount attribute",
               h.get("DeliveredMessageCount").equals(new Integer(0)));
    assertTrue("Bad value for PendingMessageCount attribute",
               h.get("PendingMessageCount").equals(new Integer(0)));
    assertTrue("Bad value for Period attribute",
               h.get("Period").equals(new Long(-1)));
    assertTrue("Bad value for NbMaxMsg attribute",
               h.get("NbMaxMsg").equals(new Integer(-1)));
    assertTrue("Bad value for Threshold attribute",
               h.get("Threshold").equals(new Integer(3)));
    assertTrue("Bad value for DestinationId attribute",
               h.get("DestinationId").equals(queue1.getName()));
    assertTrue("Bad value for DMQId attribute",
               h.get("DMQId").equals(dmq2.getName()));
//    System.out.println(h);
    
    // Verify the other queue
    Queue queue2X = Queue.create(sid, "queue2");
    assertTrue("Bad queue2", queue2.equals(queue2X));
    assertTrue("Bad read rights for queue2", queue2X.isFreelyReadable());
    readers = queue2X.getReaders();
    assertTrue("Bad readers list size for queue", (readers.size() == 0));
    assertTrue("Bad write rights for queue2", queue2X.isFreelyWriteable());
    writers = queue2X.getWriters();
    assertTrue("Bad writers list size for queue", (writers.size() == 0));
    assertTrue("Bad NbMxMsg for queue2", (queue2X.getNbMaxMsg() == 2));
    
    h = queue2X.getStatistics();
    assertTrue("Bad value for Name attribute", h.get("Name").equals(queue2.getAdminName()));
    assertTrue("CreationDate attribute missing", h.containsKey("CreationDate"));
    assertTrue("Bad value for NbMsgsDeliverSinceCreation attribute",
               h.get("NbMsgsDeliverSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsReceiveSinceCreation attribute",
               h.get("NbMsgsReceiveSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsSentToDMQSinceCreation attribute",
               h.get("NbMsgsSentToDMQSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for WaitingRequestCount attribute",
               h.get("WaitingRequestCount").equals(new Integer(0)));
    assertTrue("Bad value for DeliveredMessageCount attribute",
               h.get("DeliveredMessageCount").equals(new Integer(0)));
    assertTrue("Bad value for PendingMessageCount attribute",
               h.get("PendingMessageCount").equals(new Integer(0)));
    assertTrue("Bad value for Period attribute",
               h.get("Period").equals(new Long(-1)));
    assertTrue("Bad value for NbMaxMsg attribute",
               h.get("NbMaxMsg").equals(new Integer(2)));
    assertTrue("Bad value for Threshold attribute",
               h.get("Threshold").equals(new Integer(-1)));
    assertTrue("Bad value for DestinationId attribute",
               h.get("DestinationId").equals(queue2.getName()));
    assertFalse("Useless DMQId attribute", h.containsKey("DMQId"));
//    System.out.println(h);
    
    // Verify the topic configuration
    Topic topicX = Topic.create(sid, "topic");
    assertTrue("Bad topic", topic.equals(topicX));
    readers = topicX.getReaders();
    assertTrue("Bad readers list size for topic", (readers.size() == 1));
    assertTrue("Bad readers list for topic", readers.contains(user));
    writers = topicX.getWriters();
    assertTrue("Bad writers list size for topic", (writers.size() == 1));
    assertTrue("Bad writers list for topic", writers.contains(user));
    assertTrue("Bad DMQ for topic", topicX.getDMQ().equals(dmq2));
    
    h = topicX.getStatistics();
    assertTrue("Bad value for Name attribute", h.get("Name").equals(topic.getAdminName()));
    assertTrue("CreationDate attribute missing", h.containsKey("CreationDate"));
    assertTrue("Bad value for NbMsgsDeliverSinceCreation attribute",
               h.get("NbMsgsDeliverSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsReceiveSinceCreation attribute",
               h.get("NbMsgsReceiveSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for NbMsgsSentToDMQSinceCreation attribute",
               h.get("NbMsgsSentToDMQSinceCreation").equals(new Long(0)));
    assertTrue("Bad value for Period attribute",
               h.get("Period").equals(new Long(-1)));
    assertTrue("Bad value for DestinationId attribute",
               h.get("DestinationId").equals(topic.getName()));
    assertTrue("Bad value for DMQId attribute",
               h.get("DMQId").equals(dmq2.getName()));
//    System.out.println(h);

    Destination[] destinations = AdminModule.getDestinations(sid);
    
    boolean queue1Found = false;
    boolean queue2Found = false;
    boolean topicFound = false;
    boolean dmq1Found = false;
    boolean dmq2Found = false;
    for (int i = 0; i < destinations.length; i++) {
      Destination dest = destinations[i];
      if (dest.equals(queue1)) 
        queue1Found = true;
      else if (dest.equals(queue2)) 
        queue2Found = true;
      else if (dest.equals(topic))
        topicFound = true;
      else if (dest.equals(dmq1))
        dmq1Found = true;
      else if (dest.equals(dmq2))
        dmq2Found = true;
    }

    assertTrue("queue1 not found", queue1Found);
    assertTrue("queue2 not found", queue2Found);
    assertTrue("topic not found", topicFound);
    assertTrue("dmq1 not found", dmq1Found);
    assertTrue("dmq2 not found", dmq2Found);

    AdminModule.disconnect();

    Connection connection = cf.createConnection("anonymous", "anonymous");
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    MessageProducer producer = session.createProducer(null);
    MessageConsumer consumer1 = session.createConsumer(dmq1);      
    MessageConsumer consumer2 = session.createConsumer(dmq2);

    Session session2 = connection.createSession(true, Session.SESSION_TRANSACTED);
    MessageConsumer consumer3 = session2.createConsumer(queue1);
    MessageConsumer consumer4 = session2.createConsumer(queue2);

    connection.start();

    // Send a message that will be redirected to dmq2 (TTL)
    Message msg1 = session.createMessage();
    producer.send(queue1, msg1, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, 500L);
    
    Thread.sleep(1000L);
    
    Message msg = consumer3.receiveNoWait();
    assertTrue("Message on queue1, should be deleted by TTL", (msg == null));

    // Verify that the right message is available on dmq2
    msg = consumer2.receive(1000L);
    assertTrue("No message on dmq2", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq2", msg.getJMSMessageID().equals(msg1.getJMSMessageID()));
    
    // Send a message that will be redirected to dmq1 (TTL, default DMQ)
    Message msg2 = session.createMessage();
    producer.send(queue2, msg2, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, 500L);
    
    Thread.sleep(1000L);

    msg = consumer4.receiveNoWait();
    assertTrue("Message on queue2, should be deleted by TTL", (msg == null));

    // Verify that the right message is available on dmq1
    msg = consumer1.receive(2000L);
    assertTrue("No message on dmq1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq1", msg.getJMSMessageID().equals(msg2.getJMSMessageID()));
    
    // Send a message that will be redirected to dmq2 (threshold)
    Message msg3 = session.createMessage();
    producer.send(queue1, msg3);
    
    msg = consumer3.receive(1000L);
    assertTrue("No message on queue1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on queue1", msg.getJMSMessageID().equals(msg3.getJMSMessageID()));
    session2.rollback();
    
    msg = consumer3.receive(1000L);
    assertTrue("No message on queue1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on queue1", msg.getJMSMessageID().equals(msg3.getJMSMessageID()));
    session2.rollback();
    
    msg = consumer3.receive(1000L);
    assertTrue("No message on queue1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on queue1", msg.getJMSMessageID().equals(msg3.getJMSMessageID()));
    session2.rollback();
    
    // Verify that the right message is now available on dmq2
    msg = consumer2.receive(1000L);
    assertTrue("No message on dmq2", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq2", msg.getJMSMessageID().equals(msg3.getJMSMessageID()));
    
    // Send a message that will be redirected to dmq1 (default threshold)
    Message msg4 = session.createMessage();
    producer.send(queue2, msg4);
    
    msg = consumer4.receive(1000L);
    assertTrue("No message on queue2", (msg != null));
    if (msg != null)
      assertTrue("Bad message on queue2", msg.getJMSMessageID().equals(msg4.getJMSMessageID()));
    session2.rollback();
    
    // Verify that the right message is now available on dmq1
    msg = consumer1.receive(1000L);
    assertTrue("No message on dmq1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq1", msg.getJMSMessageID().equals(msg4.getJMSMessageID()));
    
    // Send 3 messages on queue2, the third will be redirected to dmq1 (NbMaxMessage)
    Message msg5 = session.createMessage();
    producer.send(queue2, msg5);
    Message msg6 = session.createMessage();
    producer.send(queue2, msg6);
    Message msg7 = session.createMessage();
    producer.send(queue2, msg7);

    // Verify that the last message is now available on dmq1
    msg = consumer1.receive(1000L);
    assertTrue("No message on dmq1", (msg != null));
    if (msg != null)
      assertTrue("Bad message on dmq1", msg.getJMSMessageID().equals(msg7.getJMSMessageID()));

    connection.close();

    // Verify that a subscriber is present after adding it to a topic
    AdminModule.connect(cf);
    assertEquals(0, topic.getSubscriptions());
    assertEquals(0, topic.getSubscriberIds().length);

    Connection cnx = cf.createConnection("XXX", "XXX");
    Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cnx.start();
    sessionc.createConsumer(topic);
    assertEquals(1, topic.getSubscriptions());
    assertEquals(1, topic.getSubscriberIds().length);

    cnx.close();
    assertEquals(0, topic.getSubscriptions());
    assertEquals(0, topic.getSubscriberIds().length);
    AdminModule.disconnect();

  }
}
