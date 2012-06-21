/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.bridgejms;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;

import framework.TestCase;

/*
 * Test Joram distribution bridge change mode async on and off.
 * 
 * First test : distributionQueue
 *   set async mode ON, send messages. 
 *   Verify that all messages sent are received.
 * 
 *   set async mode OFF, send messages. 
 *   Verify that all messages sent are received.
 * 
 *   set async mode ON, send messages. 
 *   Verify that all messages sent are received.
 *   
 * Second test : distributionTopic
 *   set async mode ON, send messages. 
 *   Verify that all messages sent are received.
 * 
 *   set async mode OFF, send messages. 
 *   Verify that all messages sent are received.
 * 
 *   set async mode ON, send messages. 
 *   Verify that all messages sent are received.
 * 
 */
public class AsyncBridgeTest extends TestCase {

	static final int MSG_COUNT = 10;
	static final Object lock = new Object();

  public static void main(String[] args) {
    new AsyncBridgeTest().run();
  }

  public void run() {
    try {
    	System.out.println("servers start");
    	startAgentServer((short)0);
    	startAgentServer((short)1);
    	Thread.sleep(8000);

    	// admin
    	admin();
    	System.out.println("admin config ok");

    	// test with distribution Queue
    	test("joramQueue");
    	
    	Thread.sleep(1000);
    	
    	// test with distribution Topic
    	test("joramTopic");
    	
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      killAgentServer((short)1);
      endTest(); 
    }
  }
  
  public void test(String destName) throws Exception {
    System.out.println("\n\n" + destName + ": Async ON...\n");

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    Destination joramDest = (Destination) jndiCtx.lookup(destName);
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");

    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
    jndiCtx.close();

    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer joramSender = joramSess.createProducer(joramDest);


    Connection foreignCnx = foreignCF.createConnection();
    Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
    MsgListenerForeign foreignListener = new MsgListenerForeign();
    foreignCons.setMessageListener(foreignListener);
    foreignCnx.start();
    joramCnx.start();

    TextMessage msg = joramSess.createTextMessage();

    for (int i = 0; i < MSG_COUNT; i++) {
      msg.setText("Joram message number " + i);
      System.out.println("send msg = " + msg.getText());
      joramSender.send(msg);
    }

    assertEquals("The foreign listener count", 0, foreignListener.count);
    
    synchronized (lock) {
    	lock.wait(30000);
    }
    assertEquals("The foreign listener received", MSG_COUNT, foreignListener.count);
    Thread.sleep(1000);
    
    // set async OFF
    System.out.println("\n\n" + destName + ": Async OFF...\n");
    AdminModule.connect(joramCF);
    Properties prop = new Properties();
    prop.setProperty("jms.DestinationName", "foreignQueue");
    prop.put("distribution.async", "false");
    prop.put("period", Long.toString(1000));
    prop.put("jms.ConnectionUpdatePeriod", Long.toString(500));
    joramDest.setProperties(prop);
    AdminModule.disconnect();
    Thread.sleep(1000);
    
    for (int i = MSG_COUNT; i < 2*MSG_COUNT; i++) {
      msg.setText("Joram message number " + i);
      System.out.println("send msg = " + msg.getText());
      joramSender.send(msg);
    }
    assertFalse("The foreign listener count expected " + MSG_COUNT  + " but was " + foreignListener.count, MSG_COUNT == foreignListener.count);
    
    if (foreignListener.count < 2*MSG_COUNT) {
    	synchronized (lock) {
    		lock.wait(10000);
    	}
    }
    assertEquals("The foreign listener received", 2*MSG_COUNT, foreignListener.count);
    Thread.sleep(1000);
    
    // set async ON
    System.out.println("\n\n" + destName + ": Async ON...\n");
    AdminModule.connect(joramCF);
    prop = new Properties();
    prop.setProperty("jms.DestinationName", "foreignQueue");
    prop.put("distribution.async", "true");
    prop.put("period", Long.toString(1000));
    joramDest.setProperties(prop);
    AdminModule.disconnect();
    Thread.sleep(1000);
    
    for (int i = 2*MSG_COUNT; i < 3*MSG_COUNT; i++) {
      msg.setText("Joram message number " + i);
      System.out.println("send msg = " + msg.getText());
      joramSender.send(msg);
    }
    assertTrue("The foreign listener count expected " + 2*MSG_COUNT  + " but was " + foreignListener.count, 2*MSG_COUNT == foreignListener.count);
    
    if (foreignListener.count < 3*MSG_COUNT) {
    	synchronized (lock) {
    		lock.wait(30000);
    	}
    }
    assertEquals("The foreign listener received", 3*MSG_COUNT, foreignListener.count);
    
    foreignCnx.close();
    joramCnx.close();
  }
  
  public void admin() {
    try {
      AdminModule.connect("root", "root", 60);
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();

      User user0 = User.create("anonymous", "anonymous", 0);
      User user1 = User.create("anonymous", "anonymous", 1);

      // create The foreign destination
      Queue foreignQueue = Queue.create(1, "foreignQueue");
      foreignQueue.setFreeReading();
      foreignQueue.setFreeWriting();
      user1.addInterceptorsIN("joram.bridge.SlowInterceptor");
      System.out.println("foreign queue = " + foreignQueue);
      
      // create The foreign connection factory
      javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

      // bind foreign destination and connectionFactory
      jndiCtx.rebind("foreignQueue", foreignQueue);
      jndiCtx.rebind("foreignCF", foreignCF);

      // Setting the Queue bridge properties
      Properties prop = new Properties();
      prop.setProperty("jms.DestinationName", "foreignQueue");
      prop.put("distribution.async", "true");
      prop.put("jms.ConnectionUpdatePeriod", Long.toString(500));
      prop.put("period", Long.toString(1000));
      prop.setProperty("distribution.className", JMSDistribution.class.getName());
      // Creating a Queue bridge on server 0:
      Queue joramQueue = Queue.create(0, Queue.DISTRIBUTION_QUEUE, prop);
      joramQueue.setFreeWriting();
      System.out.println("joram queue = " + joramQueue);
      
      // Setting the Topic bridge properties
      prop = new Properties();
      prop.setProperty("jms.DestinationName", "foreignQueue");
      prop.put("distribution.async", "true");
      prop.put("jms.ConnectionUpdatePeriod", Long.toString(500));
      prop.put("period", Long.toString(1000));
      prop.setProperty("distribution.className", JMSDistribution.class.getName());
      // Creating a Topic bridge on server 0:
      Topic joramTopic = Topic.create(0, Topic.DISTRIBUTION_TOPIC, prop);
      joramTopic.setFreeWriting();
      System.out.println("joram topic = " + joramTopic);

      javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();

      jndiCtx.rebind("joramQueue", joramQueue);
      jndiCtx.rebind("joramTopic", joramTopic);
      jndiCtx.rebind("joramCF", joramCF);

      jndiCtx.close();

      AdminModule.disconnect();
      System.out.println("Admin closed.");
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
  
  class MsgListenerForeign implements MessageListener {
    int count = 0;

    public void onMessage(Message msg) {
      try {
        count++;
        System.out.println("receive msg = " + ((TextMessage) msg).getText());
        if (count%MSG_COUNT == 0) {
        	synchronized (lock) {
        		lock.notify();
        	}
        }
      } catch (JMSException exc) {
        System.err.println("Exception in listener: " + exc);
      }
    }
  }
}

