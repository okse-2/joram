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

/**
 * Test :
 *    
 */



public class BridgeTest6 extends TestCase {

   
    public static void main(String[] args) {
	new BridgeTest6().run();
    }
          
    public void run() {
	try {
	    System.out.println("servers start");
	    startAgentServer((short)0);
	   
	    startAgentServer((short)1);
	    Thread.sleep(8000);
	    System.out.println("admin config ok");
	    
	    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	    Destination joramDest = (Destination) jndiCtx.lookup("joramTopic");
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
	    //  foreignCons.setMessageListener(new MsgListenerBT3("topic foreign"));
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
	     

	     Thread.sleep(3000);
   
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

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    
	User.create("anonymous", "anonymous", 0);
	User.create("anonymous", "anonymous", 1);
    
	Topic foreignTopic = Topic.create(1, "foreignTopic");
	foreignTopic.setFreeReading();
	foreignTopic.setFreeWriting();
	System.out.println("foreign topic = " + foreignTopic);
    
	javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
    
	// bind foreign destination and connectionFactory
	jndiCtx.rebind("foreignTopic", foreignTopic);
	jndiCtx.rebind("foreignCF", foreignCF);
    
    
	// Setting the bridge properties
	Properties prop = new Properties();
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

	
	jndiCtx.rebind("joramTopic", joramTopic);
	jndiCtx.rebind("joramCF", joramCF);
    
	jndiCtx.close();
	AdminModule.disconnect();
    }
}

