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
package joram.cluster;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Sends message on a clustered topic and verify that messages are received
 * trough all cluster's members.
 */
public class TestT1 extends TestCase {

  public static void main(String[] args) {
    new TestT1().run();
  }

  public void run() {
    try {
      // Starts the 3 servers.
      startAgentServer((short)0);
      startAgentServer((short)1);
      startAgentServer((short)2);

      Thread.sleep(2000);

      // Creates users and destinations, then registers them in JNDI.
      admin();

      // Gets the connection factories and destination in JNDI
      Context  ictx = new InitialContext();
      
      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
      ConnectionFactory cf2 = (ConnectionFactory) ictx.lookup("cf2");
      
      Topic dest0 = (Topic) ictx.lookup("top0");
      Topic dest1 = (Topic) ictx.lookup("top1");
      Topic dest2 = (Topic) ictx.lookup("top2");
      
      ictx.close();

      // Opens the connection with server#0, creates 2 sessions one for the producer,
      // one for the first consumer.
      Connection cnx0 = cf0.createConnection("user0", "pass");
      Session sess0a = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub = sess0a.createProducer(dest0);
      Session sess0b = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub0 = sess0b.createConsumer(dest0);
      // Starts the connection
      cnx0.start();

      // Opens the connection with server#1, creates 1 session for the second consumer.
      Connection cnx1 = cf1.createConnection("user1", "pass");
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub1 = sess1.createConsumer(dest1);
      // Starts the connection
      cnx1.start();

      // Opens the connection with server#2, creates 1 session for the third consumer.
      Connection cnx2 = cf2.createConnection("user2", "pass");
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub2 = sess2.createConsumer(dest2);
      // Starts the connection
      cnx2.start();

      TextMessage msgs[] = new TextMessage[10];
      // Sends 10 messages on topic0
      for (int i = 0; i < 10; i++) {
        msgs[i] = sess0a.createTextMessage("Msg " + i);
        pub.send(msgs[i]);
      }

      TextMessage msg = null;
      // Receives the messages through topic0
      for (int i = 0; i < 10; i++) {
        msg = (TextMessage) sub0.receive();
        assertEquals(msgs[i].getJMSMessageID(), msg.getJMSMessageID());
        assertEquals(msgs[i].getText(), msg.getText());
      }
      msg = (TextMessage) sub0.receive(1000);
      assertNull(msg);
      cnx0.close();

      // Receives the messages through topic1
      for (int i = 0; i < 10; i++) {
        msg = (TextMessage) sub1.receive();
        assertEquals(msgs[i].getJMSMessageID(), msg.getJMSMessageID());
        assertEquals(msgs[i].getText(), msg.getText());
      }
      msg = (TextMessage) sub1.receive(1000);
      assertNull(msg);
      cnx1.close();

      // Receives the messages through topic2
      for (int i = 0; i < 10; i++) {
        msg = (TextMessage) sub2.receive();
        assertEquals(msgs[i].getJMSMessageID(), msg.getJMSMessageID());
        assertEquals(msgs[i].getText(), msg.getText());
      }
      msg = (TextMessage) sub2.receive(1000);
      assertNull(msg);
      cnx2.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      stopAgentServer((short)1);
      stopAgentServer((short)2);
      endTest(); 
    }
  }


  public void admin() throws Exception {
    AdminModule.connect("root", "root", 60);

    User.create("user0", "pass", 0);
    User.create("user1", "pass", 1);
    User.create("user2", "pass", 2); 

    javax.jms.ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);
    javax.jms.ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 16011);
    javax.jms.ConnectionFactory cf2 = TcpConnectionFactory.create("localhost", 16012);

    Topic top0 = Topic.create(0);
    Topic top1 = Topic.create(1);
    Topic top2 = Topic.create(2);

    top0.setFreeReading();
    top1.setFreeReading();
    top2.setFreeReading();
    top0.setFreeWriting();
    top1.setFreeWriting();
    top2.setFreeWriting();

    top0.addClusteredTopic(top1);
    top0.addClusteredTopic(top2);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.bind("top0", top0);
    jndiCtx.bind("top1", top1);
    jndiCtx.bind("top2", top2);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}

