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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Sends message on a clustered topic and verify that messages are
 * received trough all cluster's members. Use clustering and hierarchy.
 */
public class TestT3 extends TestCase {

  private MsgListenerCluster listener0;
  private MsgListenerCluster listener1;
  private MsgListenerCluster listener2;
  private MsgListenerCluster listener3;

  public static void main(String[] args) {
    new TestT3().run();
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
      Topic dest3 = (Topic) ictx.lookup("top3");
      
      ictx.close();

      // Opens the connection with server#0
      Connection cnx0 = cf0.createConnection("user0", "pass");
      Session sess0p = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub0 = sess0p.createProducer(dest0);
      Session sess0c = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub0 = sess0c.createConsumer(dest0);
      listener0 = new MsgListenerCluster();
      sub0.setMessageListener(listener0);
      Session sess3p = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub3 = sess3p.createProducer(dest3);
      Session sess3c = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub3 = sess3c.createConsumer(dest3);
      listener3 = new MsgListenerCluster();
      sub3.setMessageListener(listener3);
      // Starts the connection
      cnx0.start();

      // Opens the connection with server#1
      Connection cnx1 = cf1.createConnection("user1", "pass");
      Session sess1p = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub1 = sess1p.createProducer(dest1);
      Session sess1c = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub1 = sess1c.createConsumer(dest1);
      listener1 = new MsgListenerCluster();
      sub1.setMessageListener(listener1);
      // Starts the connection
      cnx1.start();

      // Opens the connection with server#2
      Connection cnx2 = cf2.createConnection("user2", "pass");
      Session sess2p = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub2 = sess2p.createProducer(dest2);
      Session sess2c = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer sub2 = sess2c.createConsumer(dest2);
      listener2 = new MsgListenerCluster();
      sub2.setMessageListener(listener2);
      // Starts the connection
      cnx2.start();

      // Publish on 0, only cluster friend 1 should receive
      Message msg = sess0p.createMessage();

      for (int i = 0; i < 10; i++) {
        pub0.send(msg);
      }
      Thread.sleep(1000);

      assertEquals(10, listener0.nbMsg);
      assertEquals(10, listener1.nbMsg);
      assertEquals(0, listener2.nbMsg);
      assertEquals(0, listener3.nbMsg);
      reset();

      // Publish on 1, only cluster friend 0 should receive
      msg = sess1p.createMessage();

      for (int i = 0; i < 10; i++) {
        pub1.send(msg);
      }
      Thread.sleep(1000);

      assertEquals(10, listener0.nbMsg);
      assertEquals(10, listener1.nbMsg);
      assertEquals(0, listener2.nbMsg);
      assertEquals(0, listener3.nbMsg);
      reset();

      // Publish on 2, everyone should receive
      msg = sess2p.createMessage();

      for (int i = 0; i < 10; i++) {
        pub2.send(msg);
      }
      Thread.sleep(1000);

      assertEquals(10, listener0.nbMsg);
      assertEquals(10, listener1.nbMsg);
      assertEquals(10, listener2.nbMsg);
      assertEquals(10, listener3.nbMsg);
      reset();

      // Publish on 3, everyone should receive
      msg = sess3p.createMessage();

      for (int i = 0; i < 10; i++) {
        pub3.send(msg);
      }
      Thread.sleep(1000);

      assertEquals(10, listener0.nbMsg);
      assertEquals(10, listener1.nbMsg);
      assertEquals(10, listener2.nbMsg);
      assertEquals(10, listener3.nbMsg);
      reset();

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


  private void reset() {
    listener0.nbMsg = 0;
    listener1.nbMsg = 0;
    listener2.nbMsg = 0;
    listener3.nbMsg = 0;
  }

  public void admin() throws Exception {
    AdminModule.connect("root", "root", 60);

    User.create("user0", "pass", 0);
    User.create("user1", "pass", 1);
    User.create("user2", "pass", 2); 

    javax.jms.ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);
    javax.jms.ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 16011);
    javax.jms.ConnectionFactory cf2 = TcpConnectionFactory.create("localhost", 16012);

    // Create a hierarchical cluster:
    // (0-1)
    //  |
    // (2-3)
    Topic top0 = Topic.create(0);
    Topic top1 = Topic.create(1);
    Topic top2 = Topic.create(2);
    Topic top3 = Topic.create(0);

    top0.setFreeReading();
    top1.setFreeReading();
    top2.setFreeReading();
    top3.setFreeReading();
    top0.setFreeWriting();
    top1.setFreeWriting();
    top2.setFreeWriting();
    top3.setFreeWriting();

    top0.addClusteredTopic(top1);
    top2.addClusteredTopic(top3);
    top2.setParent(top0);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.bind("top0", top0);
    jndiCtx.bind("top1", top1);
    jndiCtx.bind("top2", top2);
    jndiCtx.bind("top3", top3);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  class MsgListenerCluster implements MessageListener {
    public int nbMsg;

    public MsgListenerCluster() {
      nbMsg = 0;
    }

    public void onMessage(Message msg) {
      nbMsg++;
    }
  }
}

