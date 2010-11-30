/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package joram.admin;

import java.util.Vector;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Verify the construction of a cluster of topic.
 * 
 * Be careful, currently subscriptions to multiples nodes of a cluster
 * doesn't work for a user.
 */
public class AdminTest4 extends TestCase {

  public static void main(String[] args) {
    new AdminTest4().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      
      Thread.sleep(1000);
      
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

    // Create the anonymous user needed for test
    User.create("anonymous", "anonymous");
    User.create("anonymous2", "anonymous2");
    User.create("anonymous3", "anonymous3");
    User.create("anonymous4", "anonymous4");
    User.create("anonymous5", "anonymous5");
    
    // Create topics and configure them
    Topic topic1 = Topic.create(sid, "topic1");
    topic1.setFreeReading();
    topic1.setFreeWriting();

    Topic topic2 = Topic.create(sid, "topic2");
    topic2.setFreeReading();
    topic2.setFreeWriting();

    Topic topic3 = Topic.create(sid, "topic3");
    topic3.setFreeReading();
    topic3.setFreeWriting();

    Topic topic4 = Topic.create(sid, "topic4");
    topic4.setFreeReading();
    topic4.setFreeWriting();

    Topic topic5 = Topic.create(sid, "topic5");
    topic5.setFreeReading();
    topic5.setFreeWriting();

    // Add a topic to itself
    try {
      topic1.addClusteredTopic(topic1);
    } catch(Exception exc) {
      fail("An exception should not be throwed");
    }
    assertEquals(1, topic1.getClusterFellows().size());
    
    // Add topic2 to topic1
    try {
      topic1.addClusteredTopic(topic2);
    } catch(Exception exc) {
      fail("An exception should not be throwed");
    }

    // Add topic3 to topic2
    try {
      topic2.addClusteredTopic(topic3);
    } catch(Exception exc) {
      fail("An exception should not be throwed");
    }
    
    // Adding a clustered topic (topic1) to topic4
    try {
      topic4.addClusteredTopic(topic1);
      topic4.removeFromCluster();
    } catch(Exception exc) {
      fail("An exception should not be throwed");
    }
    
    // Add topic5 to topic1 then remove it
    boolean isException = false;
    try {
      topic1.addClusteredTopic(topic5);
      topic5.removeFromCluster();
    } catch(Exception exc) {
      isException = true;
      exc.printStackTrace();
    }
    assertFalse("An exception should not be throwed", isException);
    
    Vector list = new Vector();
    list.add(topic1);
    list.add(topic2);
    list.add(topic3);
    
    assertTrue("topic1 cluster should contain topic1, topic2 and topic3",
               topic1.getClusterFellows().containsAll(list) && list.containsAll(topic1.getClusterFellows()));
    assertTrue("topic2 cluster should contain topic1, topic2 and topic3",
               topic2.getClusterFellows().containsAll(list) && list.containsAll(topic2.getClusterFellows()));
    assertTrue("topic3 cluster should contain topic1, topic2 and topic3",
               topic3.getClusterFellows().containsAll(list) && list.containsAll(topic3.getClusterFellows()));
    assertEquals("topic4 should be alone", 1, topic4.getClusterFellows().size());
    assertEquals("topic4 should be alone", topic4, topic4.getClusterFellows().get(0));
    assertEquals("topic5 should be alone", 1, topic5.getClusterFellows().size());
    assertEquals("topic5 should be alone", topic5, topic5.getClusterFellows().get(0));
       
    AdminModule.disconnect();
    
    Connection connection = cf.createConnection("anonymous", "anonymous");
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = session.createProducer(null);
    
    Connection connection1 = cf.createConnection("anonymous", "anonymous");
    Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer1 = session1.createConsumer(topic1);
    
    Connection connection2 = cf.createConnection("anonymous2", "anonymous2");
    Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer2 = session2.createConsumer(topic2);
    
    Connection connection3 = cf.createConnection("anonymous3", "anonymous3");
    Session session3 = connection3.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer3 = session3.createConsumer(topic3);
    
    Connection connection4 = cf.createConnection("anonymous4", "anonymous4");
    Session session4 = connection4.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer4 = session4.createConsumer(topic4);
    
    Connection connection5 = cf.createConnection("anonymous5", "anonymous5");
    Session session5 = connection5.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer5 = session5.createConsumer(topic5);

    connection.start();
    connection1.start();
    connection2.start();
    connection3.start();
    connection4.start();
    connection5.start();
    
    // Send a message to topic1, verify that is is received by all cluster
    Message msg1 = session.createMessage();
    producer.send(topic1, msg1);
    
    Message msg = consumer1.receive(500L);
    assertTrue("Message should be received on topic1", (msg != null));
    
    msg = consumer2.receive(500L);
    assertTrue("Message should be received on topic2", (msg != null));
    
    msg = consumer3.receive(500L);
    assertTrue("Message should be received on topic3", (msg != null));
    
    msg = consumer4.receive(500L);
    assertTrue("Message should not be received on topic4", (msg == null));
    
    // Send a message to topic2, verify that is is received by all cluster
    Message msg2 = session.createMessage();
    producer.send(topic2, msg2);
    
    msg = consumer1.receive(500L);
    assertTrue("Message should be received on topic1", (msg != null));
    
    msg = consumer2.receive(500L);
    assertTrue("Message should be received on topic2", (msg != null));
    
    msg = consumer3.receive(500L);
    assertTrue("Message should be received on topic3", (msg != null));
    
    msg = consumer4.receive(500L);
    assertTrue("Message should not be received on topic4", (msg == null));
    
    // Send a message to topic3, verify that is is received by all cluster
    Message msg3 = session.createMessage();
    producer.send(topic3, msg3);
    
    msg = consumer1.receive(500L);
    assertTrue("Message should be received on topic1", (msg != null));
    
    msg = consumer2.receive(500L);
    assertTrue("Message should be received on topic2", (msg != null));
    
    msg = consumer3.receive(500L);
    assertTrue("Message should be received on topic3", (msg != null));
    
    msg = consumer4.receive(500L);
    assertTrue("Message should not be received on topic4", (msg == null));
    
    // Send a message to topic4, verify that is is received only by topic4
    Message msg4 = session.createMessage();
    producer.send(topic4, msg4);
    
    msg = consumer1.receive(500L);
    assertTrue("Message should not be received on topic1", (msg == null));
    
    msg = consumer2.receive(500L);
    assertTrue("Message should not be received on topic2", (msg == null));
    
    msg = consumer3.receive(500L);
    assertTrue("Message should not be received on topic3", (msg == null));
    
    msg = consumer4.receive(500L);
    assertTrue("Message should be received on topic4", (msg != null));
    
    msg = consumer5.receive(500L);
    assertTrue("No message should be received on topic5", (msg == null));
    
    connection.close();
    connection1.close();
    connection2.close();
    connection3.close();
    connection4.close();
    connection5.close();
  }
}