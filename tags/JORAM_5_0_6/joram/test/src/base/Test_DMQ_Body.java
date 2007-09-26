

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
package base;

import framework.*;

import java.io.*;
import javax.jms.*;
import javax.naming.*;
import java.util.Enumeration;

import org.objectweb.joram.client.jms.admin.DeadMQueue;

/**
 * Test that a message body is not delete on a Dqueue when restart server 
 *        
 * 
 */
public class Test_DMQ_Body extends TestCase {

   
    public static void main(String[] args) {
	new Test_DMQ_Body().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	    
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Queue queue = (Queue) ictx.lookup("queue");
	    DeadMQueue dq =(DeadMQueue) ictx.lookup("dq");

   	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();

	    Connection cnx = cf.createConnection();

	    // create a connection for the deadqueue
	    Connection cnx_dead = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    // create a session for the deadqueue
	    Session sessioncdead = cnx_dead.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	   cnx_dead.start();
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    MessageConsumer consumer = sessionc.createConsumer(queue);
	    // create a consumer attach to the deadqueue
	   
	    // create a message send to the queue by the pruducer 
	    TextMessage msg = sessionp.createTextMessage("test not delete");
	    producer.setTimeToLive(1000L);
	    producer.send(msg);
	    
	    // sleep to be safe that the message expired
	    Thread.sleep(1000L);
	    

 
	    QueueBrowser browserDmq = sessioncdead.createBrowser(dq);
	    Enumeration messages = browserDmq.getEnumeration();
	    
	    while (messages.hasMoreElements()) {
		TextMessage msg1 = (TextMessage) messages.nextElement();
		if(msg1 != null){
		    // System.out.println("message expired !");
		    //test messages
		 assertEquals(true, msg1.getBooleanProperty("JMS_JORAM_EXPIRED"));
		 assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
		 assertEquals(msg.getJMSType(),msg1.getJMSType());
		 assertEquals("test not delete",msg1.getText());
		 
		}
	    }
    
	    cnx.close();
	    stopAgentServer((short)0);
	    Thread.sleep(2000);
	    startAgentServer((short)0);
	    ictx = new InitialContext();
	    dq =(DeadMQueue) ictx.lookup("dq");
	    cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();
	    
	    cnx_dead = cf.createConnection();
	    sessioncdead = cnx_dead.createSession(false,
							  Session.AUTO_ACKNOWLEDGE);
	    cnx_dead.start();

	    browserDmq = sessioncdead.createBrowser(dq);
	    messages = browserDmq.getEnumeration();
	    
	    while (messages.hasMoreElements()) {
		TextMessage msg1 = (TextMessage) messages.nextElement();
		if(msg1 != null){
		    // System.out.println("message expired !");
		    //test messages
		    assertEquals(true, msg1.getBooleanProperty("JMS_JORAM_EXPIRED"));
		    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
		    assertEquals(msg.getJMSType(),msg1.getJMSType());
		    assertEquals("test not delete",msg1.getText());
		    
		}
	    }
	    

	     
	  
	    cnx_dead.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}
	finally {
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    endTest(); 
	}
    }

    /**
     * Admin : Create queue and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
								 "root", "root", 60);
	// create a Queue   
	org.objectweb.joram.client.jms.Queue queue =
	    (org.objectweb.joram.client.jms.Queue) org.objectweb.joram.client.jms.Queue.create(0); 

	// create a deadqueue for receive expired messages
	DeadMQueue dq = (DeadMQueue) DeadMQueue.create(0);
	dq.setFreeReading();
	dq.setFreeWriting();
       	org.objectweb.joram.client.jms.admin.AdminModule.setDefaultDMQ(0,dq);

        // create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
	// set permissions
	queue.setFreeReading();
	queue.setFreeWriting();

      	javax.jms.ConnectionFactory cf =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("queue", queue);
	jndiCtx.bind("dq", dq);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}

