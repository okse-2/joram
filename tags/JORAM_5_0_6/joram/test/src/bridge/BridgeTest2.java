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

class MsgListenerb implements MessageListener {
    String who;
    int count;
    public MsgListenerb(String who) {
	System.out.println("creation");
	this.who = who;
	count=0;
    }
    
    public void onMessage(Message msg) {
	try {
	    count++;
	    TextMessage msg2=(TextMessage) msg;
	    BridgeTest2.assertEquals("Foreign message number "+count,msg2.getText());
	    System.out.println(who+" receive msg = " + msg2.getText());
	}catch (JMSException exc) {
	    System.err.println("Exception in listener: " + exc);
	}
    }
}

public class BridgeTest2 extends TestCase {
    
    
    public static void main(String[] args) {
	new BridgeTest2().run();
    }
          
    public void run() {
	try {
	    startAgentServer((short)0);
	    startAgentServer((short)1);
	    //admin();
	    Thread.sleep(8000);
	    System.out.println("admin config ok");

	    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	    Destination joramDest = (Destination) jndiCtx.lookup("joramTopic");
	    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
	  	 
	    Destination foreignDest = (Destination) jndiCtx.lookup("foreignTopic");
	    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF"); 
	    jndiCtx.close();
	    
	    Connection joramCnx = joramCF.createConnection();
	    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer joramCons = joramSess.createConsumer(joramDest);
	    //joramCons.setMessageListener(new MsgListenerb("topic joram"));
	    joramCnx.start();  

	    Connection foreignCnx = foreignCF.createConnection();
	    Session foreignSess = foreignCnx.createSession(true, 0);
	    MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
	    
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
	    
	    // System.in.read();
	    joramCnx.close();
	       foreignCnx.close();
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
	AdminModule.connect("root", "root", 60);

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
