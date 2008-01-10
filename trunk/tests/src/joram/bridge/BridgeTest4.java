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
package joram.bridge;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Test :
 *    
 */
public class BridgeTest4 extends TestCase {

   
    public static void main(String[] args) {
	new BridgeTest4().run();
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
	    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
	    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");

	    Destination joramDest = (Destination) jndiCtx.lookup("joramQueue");
	    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
	    jndiCtx.close();
	    
	    Connection foreignCnx = foreignCF.createConnection();
	    Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    Connection joramCnx = joramCF.createConnection();
	    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    
	    MessageProducer joramSender = joramSess.createProducer(joramDest);
	    MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
	    foreignCnx.start();
	    joramCnx.start(); 
	    
	    TextMessage msg = joramSess.createTextMessage();
	    
	    for (int i = 1; i < 11; i++) {
		msg.setText("Joram message number " + i);
		System.out.println("send msg = " + msg.getText());
		joramSender.send(msg);
	    }
	   
	   
	    for (int i = 1; i < 11; i++) { 
		msg=(TextMessage) foreignCons.receive();
		System.out.println("receive msg = " + msg.getText());
		assertEquals("Joram message number "+i,msg.getText());
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
     * Admin : Create queue and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	AdminModule.connect("localhost", 16010,"root", "root", 60);
	
	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	
	User.create("anonymous", "anonymous", 0);
	User.create("anonymous", "anonymous", 1);
	
	// create The foreign destination and connectionFactory
	Queue foreignQueue = Queue.create(1, "foreignQueue");
	foreignQueue.setFreeReading();
	foreignQueue.setFreeWriting();
	System.out.println("foreign queue = " + foreignQueue);
		
	javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
	
	// bind foreign destination and connectionFactory
	jndiCtx.rebind("foreignQueue", foreignQueue);
	jndiCtx.rebind("foreignCF", foreignCF);
	
	
	// Setting the bridge properties
	Properties prop = new Properties();
	// Foreign QueueConnectionFactory JNDI name: foreignCF
	prop.setProperty("connectionFactoryName", "foreignCF");
	// Foreign Queue JNDI name: foreignDest
	prop.setProperty("destinationName", "foreignQueue");
	// automaticRequest
	prop.setProperty("automaticRequest", "true");
	
	// Creating a Queue bridge on server 0:
	Queue joramQueue = Queue.create(0,
					"org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeQueue",
					prop);
	joramQueue.setFreeReading();
	joramQueue.setFreeWriting();
	System.out.println("joram queue = " + joramQueue);
	
	javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();
	
	jndiCtx.rebind("joramQueue", joramQueue);
	jndiCtx.rebind("joramCF", joramCF);
	
	jndiCtx.close();
	
	AdminModule.disconnect();
    }
}

