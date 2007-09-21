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
package bridge;

import framework.*;

import java.io.*;
import javax.jms.*;
import javax.naming.*;


import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;
/**
 * Test :
 *    
 */

public class XABridgeTest2 extends TestCase {

   
    public static void main(String[] args) {
	new XABridgeTest2().run();
    }
          
    public void run() {
	try {
	    System.out.println("servers start");
	    startAgentServer((short)0);
	    startAgentServer((short)1);
	    Thread.sleep(8000);
	    //admin();
	    System.out.println("admin config ok");

	    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	    Destination foreignDest = (Destination) jndiCtx.lookup("foreignTopic");
	    XAConnectionFactory foreignCF = (XAConnectionFactory) jndiCtx.lookup("foreignCF");

	    Destination joramDest = (Destination) jndiCtx.lookup("joramTopic");
	    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
	    jndiCtx.close();
	    Connection joramCnx = joramCF.createConnection();
	    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer joramCons = joramSess.createConsumer(joramDest);

	    Connection foreignCnx = foreignCF.createXAConnection();
	    Session foreignSess = foreignCnx.createSession(true, 0);
	    MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
	    joramCnx.start();
	    TextMessage foreignMsg = foreignSess.createTextMessage();
	    
	    for (int i = 1; i < 11; i++) {
		foreignMsg.setText("topic Foreign message number " + i);
		System.out.println("send msg = " + foreignMsg.getText());
		foreignSender.send(foreignMsg);
	    }
	    foreignSess.commit();
	   

	  
	    TextMessage msg;
	    for (int i = 1; i < 11; i++) { 
		msg=(TextMessage)joramCons.receive();
		System.out.println("receive msg = " + msg.getText());
		assertEquals("topic Foreign message number "+i,msg.getText());
	    }
	    
	  
	     
	    foreignCnx.close();
	    joramCnx.close();
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
    
    /**
     * Admin : Create topic and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	AdminModule.connect("localhost", 16010,"root", "root", 60);

	
	User.create("anonymous", "anonymous", 0);
	User.create("anonymous", "anonymous", 1);
	
	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	
	// create The foreign destination and connectionFactory
	Queue foreignQueue = Queue.create(1, "foreignQueue");
	foreignQueue.setFreeReading();
	foreignQueue.setFreeWriting();
	System.out.println("foreign queue = " + foreignQueue);
	
	Topic foreignTopic = Topic.create(1, "foreignTopic");
	foreignTopic.setFreeReading();
	foreignTopic.setFreeWriting();
	System.out.println("foreign topic = " + foreignTopic);
	
	javax.jms.XAConnectionFactory foreignCF = XATcpConnectionFactory.create("localhost", 16011);
	
	// bind foreign destination and connectionFactory
	jndiCtx.rebind("foreignQueue", foreignQueue);
	jndiCtx.rebind("foreignTopic", foreignTopic);
	jndiCtx.rebind("foreignCF", foreignCF);
	
	
	// Setting the bridge properties
	Properties prop = new Properties();
	// Foreign QueueConnectionFactory JNDI name: foreignCF
	prop.setProperty("connectionFactoryName", "foreignCF");
	// Foreign Queue JNDI name: foreignDest
	prop.setProperty("destinationName", "foreignQueue");
	// automaticRequest
	prop.setProperty("automaticRequest", "false");
	
	// Creating a Queue bridge on server 0:
	Queue joramQueue = Queue.create(0,
					"org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeQueue",
					prop);
	joramQueue.setFreeReading();
	joramQueue.setFreeWriting();
	System.out.println("joram queue = " + joramQueue);
	
	// Setting the bridge properties
	prop = new Properties();
	// Foreign QueueConnectionFactory JNDI name: foreignCF
	prop.setProperty("connectionFactoryName", "foreignCF");
	// Foreign Queue JNDI name: foreignDest
	prop.setProperty("destinationName", "foreignTopic");
	
	// Creating a Topic bridge on server 0:
	Topic joramTopic = Topic.create(0,
					"org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeTopic",
					prop);
	joramTopic.setFreeReading();
	joramTopic.setFreeWriting();
	System.out.println("joram topic = " + joramTopic);
	
	javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();
	
	jndiCtx.rebind("joramQueue", joramQueue);
	jndiCtx.rebind("joramTopic", joramTopic);
	jndiCtx.rebind("joramCF", joramCF);
	
	jndiCtx.close();
	
	AdminModule.disconnect();
    }
}

