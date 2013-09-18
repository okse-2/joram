/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2013 ScalAgent Distributed Technologies
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
package joram.sub;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;


/**
 * Test the deletion of all messages of a subscription through the clear
 * administration method (this test targets either classic and durable
 * subscription).
 */
public class Sub5 extends TestCase {
  
  public static void main(String[] args) {
    new Sub5().run();
  }

  Topic topic;
  ConnectionFactory cf;
  Connection cnx;
  Session sess1, sess2;
  User anonymous;
  String subName1;
  String subName2 = "dursub";
  
  
  public void run() {
    try {
      System.out.println("server start");
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      Thread.sleep(1000);

      cf = TcpConnectionFactory.create("localhost", 2560);

      AdminModule.connect(cf);

      // create topics
      topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      // create a user
      anonymous = User.create("anonymous", "anonymous");

      //AdminModule.disconnect();
      System.out.println("admin config ok");

      // connection for subscriber
      cnx = (Connection) cf.createConnection("anonymous", "anonymous");
      cnx.setClientID("Sub5");
      
      sess1 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess1.createProducer(topic);
      
      sess2 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      MessageConsumer cons1 = sess2.createConsumer(topic);
      subName1 = anonymous.getSubscriptions()[0].getName();
      
      MessageConsumer cons2 = sess2.createDurableSubscriber(topic, subName2);

      cnx.start();
      
      int pending1 = anonymous.getSubscription(subName1).getMessageCount();
      assertTrue("Pending-sub1-t0 " + pending1, (pending1 == 0));
      
      int pending2 = anonymous.getSubscription(subName2).getMessageCount();
      assertTrue("Pending-sub2-t0 " + pending2, (pending2 == 0));
      
      for (int i=0; i<10; i++) {
        Message msg = sess1.createMessage();
        prod.send(msg);
      }
      System.out.println("10 messages sent..");
      Thread.sleep(500L);

      pending1 = anonymous.getSubscription(subName1).getMessageCount();
      assertTrue("Pending-sub1-t1 " + pending1, (pending1 == 10));

      pending2 = anonymous.getSubscription(subName2).getMessageCount();
      assertTrue("Pending-sub2-t1 " + pending2, (pending2 == 10));
      
      anonymous.clearSubscription(subName1);
      anonymous.clearSubscription(subName2);
      
      pending1 = anonymous.getSubscription(subName1).getMessageCount();
      assertTrue("Pending-sub1-t3 " + pending1, (pending1 == 0));
      
      pending2 = anonymous.getSubscription(subName2).getMessageCount();
      assertTrue("Pending-sub2-t3 " + pending2, (pending2 == 0));

      AdminModule.disconnect();
      cnx.close();
    } catch (Throwable exc) {
		  exc.printStackTrace();
		  error(exc);
	  } finally {
		  System.out.println("Server stop");
      AgentServer.stop();
		  endTest();
	  }
  }
}
