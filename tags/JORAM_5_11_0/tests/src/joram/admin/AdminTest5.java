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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * This test verify the creation of destination through the Session interface.
 */
public class AdminTest5 extends TestCase {

  public static void main(String[] args) {
    new AdminTest5().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      Thread.sleep(2000);

      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;

      AdminModule.connect(cf, "root", "root");
      User.create("anonymous", "anonymous", 0);
      AdminModule.disconnect();
      
      Connection connection = cf.createConnection("anonymous", "anonymous");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) session.createQueue("queue");
      Topic topic = (Topic) session.createTopic("topic");
      MessageProducer producer = session.createProducer(null);
      MessageConsumer consumer1 = session.createConsumer(queue);
      MessageConsumer consumer2 = session.createConsumer(topic);
      
      // Try to create a queue with the name of a previously created topic
      boolean isException = false;
      try {
        Queue queue2 = (Queue) session.createQueue("topic");
      } catch(Exception exc) {
        isException = true;
        assertTrue("Bad exception", exc instanceof JMSException);
        assertTrue("Bad exception message", exc.getMessage().equals("Destination type not compliant"));
      }
      assertTrue("An exception should be throwed", isException);

      connection.start();

      // Send a message to the queue
      Message msg1 = session.createMessage();
      producer.send(queue, msg1);
      
      // Verify that the right message is available
      Message msg = consumer1.receive(1000L);
      assertTrue("No message on queue", (msg != null));
      if (msg != null)
        assertTrue("Bad message on queue", msg.getJMSMessageID().equals(msg1.getJMSMessageID()));

      // Send a message to the queue
      Message msg2 = session.createMessage();
      producer.send(topic, msg2);
      
      // Verify that the right message is available
      msg = consumer2.receive(1000L);
      assertTrue("No message on queue", (msg != null));
      if (msg != null)
        assertTrue("Bad message on queue", msg.getJMSMessageID().equals(msg2.getJMSMessageID()));

      AdminModule.connect(cf, "root", "root");
      Queue queueX = Queue.create("queue");
      assertTrue("Bad queue", queue.equals(queueX));
      Topic topicX = Topic.create("topic");
      assertTrue("Bad topic", topic.equals(topicX));
      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
