/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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
package joram.dursub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Tests durable subscription:
 * - Create a durable sub 'dursub' to a topic.
 * - Sends a message msg1 to the topic.
 * - Receives msg1 from the durable subscription.
 * - Sends a message msg2 to the topic.
 * - Stops the client.
 * - Starts another client that receive msg2 from the durable subscription.
 */
public class Test6 extends TestCase {

  public static void main(String[] args) {
    new Test6().run();
  }

  public void run() {
    try {
      ConnectionFactory cf = TcpConnectionFactory.create("localhost",2560 );
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous");

      Topic topic = Topic.create("topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      AdminModule.disconnect();
      
      Connection cnx = cf.createConnection("anonymous", "anonymous");

      Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess1.createProducer(topic);

      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber cons = sess2.createDurableSubscriber(topic, "dursub");
      
      cnx.start();
      
      sess2.close();

      sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess2.createDurableSubscriber(topic, "dursub");

      TextMessage msg1 = sess1.createTextMessage("msg1");
      prod.send(msg1);

      TextMessage msg2 = sess1.createTextMessage("msg2");
      prod.send(msg2);
      
      TextMessage msg = (TextMessage) cons.receive(5000L);
      assertTrue(msg != null);
      if (msg != null) {
        assertEquals(msg.getText(), msg1.getText());
      }
      Thread.sleep(500L);

      // Simulates a client failure
      endTest();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    }
  }
}
