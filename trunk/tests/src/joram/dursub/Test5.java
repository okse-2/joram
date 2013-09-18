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
 * Test durable subscription:
 * - Check that two durable subscriptions do not interfere when consuming their messages.
 */
public class Test5 extends TestCase {

  public static void main(String[] args) {
    new Test5().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      Thread.sleep(2000L);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost",2560 );
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);

      Topic topic = Topic.create(0);
      topic.setFreeReading();
      topic.setFreeWriting();
      
      AdminModule.disconnect();

      Connection cnx1 = cf.createConnection("anonymous", "anonymous");
      cnx1.setClientID("cnx_dursub1");
      Session sess = cnx1.createSession(false,Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess.createProducer(topic);

      Session sess1 = cnx1.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      TopicSubscriber cons1 = sess1.createDurableSubscriber(topic, "dursub1");
      
      cnx1.start();
      
      Connection cnx2 = cf.createConnection("anonymous", "anonymous");
      cnx2.setClientID("cnx_dursub2");
      Session sess2 = cnx2.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      TopicSubscriber cons2 = sess2.createDurableSubscriber(topic, "dursub2");
      
      cnx2.start();

      TextMessage msg = sess.createTextMessage("msg#1");
      prod.send(msg);

      TextMessage msg1 = (TextMessage) cons1.receive(5000L);
      assertTrue(msg1 != null);
      if (msg1 != null) {
        assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
        assertEquals(msg.getText(), msg1.getText());
        msg1.acknowledge();
      }
      
      TextMessage msg2 = (TextMessage) cons2.receive(5000L);
      assertTrue(msg2 != null);
      if (msg2 != null) {
        assertEquals(msg.getJMSMessageID(), msg2.getJMSMessageID());
        assertEquals(msg.getText(), msg2.getText());
        msg2.acknowledge();
      }
      
      cnx2.close();

      msg = sess.createTextMessage("msg#2");
      prod.send(msg);

      msg1 = (TextMessage) cons1.receive(5000L);
      assertTrue(msg1 != null);
      if (msg1 != null) {
        assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
        assertEquals(msg.getText(), msg1.getText());
        msg1.acknowledge();
      }

      cnx2 = cf.createConnection("anonymous", "anonymous");
      cnx2.setClientID("cnx_dursub2");
      sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons2 =  sess2.createDurableSubscriber(topic, "dursub2");
      cnx2.start();
      
      msg2 = (TextMessage) cons2.receive(5000L);
      assertTrue(msg2 != null);
      if (msg2 != null) {
        assertEquals(msg.getJMSMessageID(), msg2.getJMSMessageID());
        assertEquals(msg.getText(), msg2.getText());
      }
      
      cnx1.close();
      cnx2.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
