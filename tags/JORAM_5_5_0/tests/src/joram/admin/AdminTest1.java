/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2006 - 2009 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

public class AdminTest1 extends TestCase {

  public static void main(String[] args) {
    new AdminTest1().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      Thread.sleep(4000);
      
      System.out.println("Standard admin");
      doTest(false);
      
      stopAgentServer((short)0);
      Thread.sleep(4000);
      
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      Thread.sleep(4000);
      
      System.out.println("Multithread admin");
      doTest(true);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
  
  private void doTest(boolean multiThreadSync) throws Exception {
    Context ctx = new InitialContext();
    
    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
    ((TcpConnectionFactory) cf).getParameters().multiThreadSync = multiThreadSync;

    AdminModule.connect(cf, "root", "root");
    
    User user = User.create("anonymous", "anonymous", 0);

    // Create a queue
    Queue queue = Queue.create(0, "queue");
    queue.setFreeReading();
    queue.setFreeWriting();

    // Get the reference of an existing queue
    Queue queue2 = Queue.create(0, "queue");
    assertTrue("Bad queue", queue.equals(queue2));
    assertTrue("Bad read rights", queue2.isFreelyReadable());
    assertTrue("Bad write rights", queue2.isFreelyWriteable());

    // Create a topic
    Topic topic = Topic.create(0, "topic");

    // Get the reference of an existing topic
    Topic topic2 = Topic.create(0, "topic");
    assertTrue("Bad topic", topic.equals(topic2));

    Queue deadMQueue = Queue.create(0, "dmq");

    ((TcpConnectionFactory) cf).getParameters().multiThreadSync = multiThreadSync;

    Connection connection = cf.createConnection("anonymous", "anonymous");

    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    // Create JMS objects representing the destinations
    javax.jms.Queue jmsQueue = session.createQueue(queue.getName());
    javax.jms.Topic jmsTopic = session.createTopic(topic.getName());
    javax.jms.Queue jmsQueue2 = session.createQueue(deadMQueue.getName());

    TemporaryTopic tmpTopic = null;
    // Don't need to create a temporary topic
    // there is already one
    //session.createTemporaryTopic();
    TemporaryQueue tmpQueue = session.createTemporaryQueue();

    connection.start();

    Destination[] destinations = AdminModule.getDestinations(0);

    assertTrue("Wrong destinations count (" + destinations.length + ')', destinations.length == 5);

    boolean topicFound = false;
    boolean queueFound = false;
    boolean tmpTopicFound = false;
    boolean tmpQueueFound = false;
    boolean deadMQueueFound = false;
    for (int i = 0; i < destinations.length; i++) {
      Destination dest = destinations[i];
      if (dest.getAdminName().equals("queue")) {
        assertTrue("Wrong queue type: " + dest, dest.getClass() == Queue.class);
        queueFound = true;
      } else if (dest.getAdminName().equals("topic")) {
        assertTrue("Wrong topic type: " + dest, dest.getClass() == Topic.class);
        topicFound = true;
      } else if (dest.getAdminName().equals("dmq")) {
        deadMQueueFound = true;
      }else if (dest instanceof TemporaryTopic) {
        tmpTopic = (TemporaryTopic)dest;
        tmpTopicFound = true;
      } else if (dest instanceof TemporaryQueue) {
        tmpQueueFound = true;
      }
    }

    assertTrue("Topic not found", topicFound);
    assertTrue("Queue not found", queueFound);
    assertTrue("Tmp topic not found", tmpTopicFound);
    assertTrue("Tmp queue not found", tmpQueueFound);
    assertTrue("DeadMQueue not found", deadMQueueFound);

    ctx.bind("user", user);

    ctx.bind("queue", queue);
    ctx.bind("topic", topic);
    ctx.bind("tmpQueue", tmpQueue);
    ctx.bind("tmpTopic", tmpTopic);
    ctx.bind("deadMQueue", deadMQueue);

    user = (org.objectweb.joram.client.jms.admin.User)ctx.lookup("user");

    queue = (org.objectweb.joram.client.jms.Queue)ctx.lookup("queue");
    topic = (org.objectweb.joram.client.jms.Topic)ctx.lookup("topic");
    tmpQueue = (TemporaryQueue)ctx.lookup("tmpQueue");
    tmpTopic = (TemporaryTopic)ctx.lookup("tmpTopic");
    deadMQueue = (Queue)ctx.lookup("deadMQueue");

    MessageConsumer consumer = session.createConsumer(queue);
    MessageProducer producer = session.createProducer(queue);
    
    TextMessage msg = session.createTextMessage("adminTest1_msg#1");
    msg.setJMSDestination(queue);
    msg.setJMSReplyTo(topic);
    producer.send(msg);
    
    msg = (TextMessage)consumer.receive();
    queue = (org.objectweb.joram.client.jms.Queue)msg.getJMSDestination();
    topic = (org.objectweb.joram.client.jms.Topic)msg.getJMSReplyTo();
    
    connection.close();
    
    AdminModule.disconnect();
  }
}
