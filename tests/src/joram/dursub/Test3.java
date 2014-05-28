/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
 * Test the functioning of DurableSubscriber:
 *  - Creates a durable subscription then close the connection.
 *  - Sends a message.
 *  - Restarts the durable subscriber and receive message.
 */
public class Test3 extends TestCase  {
  public static void main(String[] args) {
    new Test3().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      Thread.sleep(1000L);

      ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);
      
      Topic topic = Topic.create();
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      // Creates a connection for messages producer
      Connection cnx1 = cf.createConnection();
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess1.createProducer(topic);
      cnx1.start();

      // Creates a connection and initializes the durable subscriber
      Connection cnx2 = cf.createConnection();
      cnx2.setClientID("cnx_dursub");
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber cons = sess2.createDurableSubscriber(topic, "dursub");  
      cnx2.start();

      // close connection of subscriber
      cnx2.close();
      
      // send a message
      TextMessage msg = sess1.createTextMessage();
      msg.setText("message_text");
      prod.send(msg);
      cnx1.close();
      
      // reconnection of subscribe
      cnx2 = cf.createConnection();
      cnx2.setClientID("cnx_dursub");
      sess2 = cnx2.createSession(false,Session.AUTO_ACKNOWLEDGE);
      cons = sess2.createDurableSubscriber(topic, "dursub");
      cnx2.start();
      // and receive the message
      TextMessage msg2 = (TextMessage) cons.receiveNoWait();
      cnx2.close();
      
      assertTrue(msg2 != null);
      if (msg2 != null) {
        assertEquals(msg.getJMSMessageID(), msg2.getJMSMessageID());
        assertEquals(msg.getText(), msg2.getText());
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}

