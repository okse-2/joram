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
package joram.noreg;

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
 * Verify that a JMS message can be received through different subscriptions
 * of a user.
 */
public class Test59 extends TestCase {

  public static void main(String[] args) {
    new Test59().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
      
      AdminModule.connect(cf, "root", "root");

      // Create the anonymous user needed for test
      User.create("anonymous", "anonymous");

      // Create topics and configure them
      Topic topic = Topic.create("topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      AdminModule.disconnect();

      Connection connection = cf.createConnection("anonymous", "anonymous");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(null);
      MessageConsumer consumer1 = session.createConsumer(topic);
      MessageConsumer consumer2 = session.createConsumer(topic);
      connection.start();
      
      // Send a message to topic, verify that is is received by consumer1 and consumer2
      Message msg1 = session.createMessage();
      producer.send(topic, msg1);
      
      Message msg = consumer1.receive(500L);
      assertTrue("Message should be received on topic1", (msg != null));
      
      msg = consumer2.receive(500L);
      assertTrue("Message should be received on topic2", (msg != null));
      
      connection.close();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
