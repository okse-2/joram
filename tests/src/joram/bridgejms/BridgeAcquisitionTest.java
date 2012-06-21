/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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

import java.io.File;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSAcquisition;

import framework.TestCase;

/*
 * Test Joram acquisition bridge with server crash:
 * Admin create :
 *  - Joram acquisition queue.
 *  - foreignQueue with slow interceptor on user out.
 *  
 *  Test1: queue distribution
 *  send nb MSG_COUNT messages, and stop the agentserver1 (foreign) on message 10. 
 *  When exception listener call restart the agentserver1.
 *  Verify that all messages sent are received.
 *  
 *  Test2: queue distribution
 *  send nb MSG_COUNT messages, and stop the agentserver0 (joram) on message 10. 
 *  When exception listener call restart the agentserver0.
 *  Verify that all messages sent are received.
 * 
 */
public class BridgeAcquisitionTest extends TestCase {

	static final int MSG_COUNT = 15;
	static final Object lock = new Object();
	ConnectionFactory joramCF;
	Destination joramDest;
	Connection joramCnx;
	Session joramSess;
	MessageConsumer joramCons;
	ConnectionFactory foreignCF;
	Connection foreignCnx;
	Session foreignSess;
	MessageProducer ForeignSender;
	Destination foreignDest;
	Destination dmQueue;
	int count;
	boolean first;
	
  public static void main(String[] args) {
    new BridgeAcquisitionTest().run();
  }

  public void run() {
  	try {
  		//TEST 1
  		test((short)1, "joramQueue", 0);

  		// reset Test
  		reset();
  		
  		//TEST 2
  		test((short)0, "joramQueue", 0);
  		
  		// reset Test
  		reset();

  	} catch (Throwable exc) {
  		exc.printStackTrace();
  		error(exc);
  	} finally {
  		System.out.println("finaly kill servers.");
  		killAgentServer((short)0);
  		killAgentServer((short)1);
  		endTest(); 
  	}
  }
  
  public void reset() throws Exception {
  	// reset Test
  	System.out.println("\nreset servers.\n");
  	killAgentServer((short)0);
  	killAgentServer((short)1);

  	Thread.sleep(3000);
		deleteDirectory(new File("s0"));
		deleteDirectory(new File("s1"));	
  }
  
  public void test(short serverToKill, String destName, int threshold) throws Exception {
  	System.out.println("\n\nTEST: kill server " + serverToKill + ", joram acquisition = " + destName + ", threshold = " + threshold);
  	System.out.println("servers start");
  	startAgentServer((short)0);
  	startAgentServer((short)1);
  	Thread.sleep(8000);

  	count = 0;
  	first = true;
  	admin();
  	System.out.println("admin config ok");

  	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
  	joramDest = (Destination) jndiCtx.lookup(destName);
  	if ((threshold > 0) && (joramDest instanceof Queue)) {
  		AdminModule.connect("root", "root", 60);
  		((Queue)joramDest).setThreshold(threshold);
  		AdminModule.disconnect();
  	}
  	joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");

  	foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
  	foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
  	
  	dmQueue = (Destination) jndiCtx.lookup("dmQueue");
  	jndiCtx.close();

  	joramCnx = joramCF.createConnection();
  	joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
  	joramCnx.setExceptionListener(new JoramListenerException());
  	joramCons = joramSess.createConsumer(joramDest);
  	JoramMsgListener joramListener = new JoramMsgListener(serverToKill);
  	joramCons.setMessageListener(joramListener);
  	
  	foreignCnx = foreignCF.createConnection();
  	foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
  	foreignCnx.setExceptionListener(new ForeignListenerException());
  	MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
  	
  	foreignCnx.start();
  	joramCnx.start();

  	TextMessage msg = foreignSess.createTextMessage();

  	for (int i = 0; i < MSG_COUNT; i++) {
  		msg.setText("Foreign message number " + i);
  		System.out.println("send msg = " + msg.getText());
  		foreignSender.send(msg);
  	}

  	assertEquals("The joram listener count", 0, count);

  	synchronized (lock) {
  		if (threshold < 1)
  			lock.wait(60000);
  		else 
  			lock.wait(30000);
  	}
  	
  	if (threshold < 1) {
  		assertEquals("The joram listener received", MSG_COUNT, count);
  	} else {
  		MessageConsumer mc = foreignSess.createConsumer(dmQueue);
  		while (true) {
  			Message m = mc.receive(3000);
  			if (m == null)
  				break;
  			System.out.println("DMQ m = " + ((TextMessage) m).getText());
  			count++;
  		}
  		assertEquals("The joram listener and DMQ received", MSG_COUNT, count);
  	}

  	foreignCnx.close();
  	joramCnx.close();

  	System.out.println("\nTEST DONE.");
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
      user1.addInterceptorsOUT("joram.bridge.SlowInterceptor");
      System.out.println("foreign queue = " + foreignQueue);
      
      // create The foreign connection factory
      javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

      // bind foreign destination and connectionFactory
      jndiCtx.rebind("foreignQueue", foreignQueue);
      jndiCtx.rebind("foreignCF", foreignCF);

      // Setting the bridge properties
      Properties prop = new Properties();
      prop.setProperty("jms.DestinationName", "foreignQueue");
      prop.put("jms.ConnectionUpdatePeriod", Long.toString(500));
      prop.put("period", Long.toString(1000));
      prop.setProperty("acquisition.className", JMSAcquisition.class.getName());
      // Creating a Queue bridge on server 0:
      Queue joramQueue = Queue.create(0, Queue.ACQUISITION_QUEUE, prop);
      joramQueue.setFreeWriting();
      joramQueue.setFreeReading();
      System.out.println("joram queue = " + joramQueue);
      
      javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();
      
      // Creating a dead message Queue on server 0:
      Queue dmQueue = Queue.create(0, Queue.DEAD_MQUEUE, null);
      dmQueue.setFreeReading();
      dmQueue.setFreeWriting();
      System.out.println("joram dmQueue = " + dmQueue);

      joramQueue.setDMQ(dmQueue);

      jndiCtx.rebind("joramQueue", joramQueue);
      jndiCtx.rebind("joramCF", joramCF);
      jndiCtx.rebind("dmQueue", dmQueue);

      jndiCtx.close();

      AdminModule.disconnect();
      System.out.println("Admin closed.");
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
  
  class JoramMsgListener implements MessageListener {

  	short serverToKill = 1;
  	
  	public JoramMsgListener(short serverToKill) {
  		this.serverToKill = serverToKill;
  	}
  	
  	public void onMessage(Message msg) {
      try {
        count++;
        System.out.println("receive msg = " + ((TextMessage) msg).getText());
        
        if (count == 10) {
        	if (first) {
        		System.out.println("kill server " + serverToKill);
        		killAgentServer(serverToKill);
        	}
        }
        
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
  
  class JoramListenerException implements ExceptionListener {

		@Override
    public void onException(JMSException exc) {
			if (!first)
				return;
	   System.out.println("=== joram onException : " + exc);
	   first = false;
	   try {
	  		Thread.sleep(8000);
	  		startAgentServer((short)0);
	  		Thread.sleep(1000);
	  		System.out.println("server 0 restarted.");

	  		Connection joramCnx = joramCF.createConnection();
	      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	      MessageConsumer joramCons = joramSess.createConsumer(joramDest);
	      JoramMsgListener joramListener = new JoramMsgListener((short)1);
	      joramCons.setMessageListener(joramListener);
	      joramCnx.setExceptionListener(new JoramListenerException());
	      joramCnx.start();
	  	} catch (Exception e) {
	  		e.printStackTrace();
	  	}
    }
  }
  
  class ForeignListenerException implements ExceptionListener {

		@Override
    public void onException(JMSException exc) {
			if (!first)
				return;
	   System.out.println("=== Foreign onException : " + exc);
	   first = false;
	   try {
	  		Thread.sleep(8000);
	  		startAgentServer((short)1);
	  		Thread.sleep(1000);
	  		System.out.println("server 1 restarted.");
	  	} catch (Exception e) {
	  		e.printStackTrace();
	  	}
    }
  	
  }
}

