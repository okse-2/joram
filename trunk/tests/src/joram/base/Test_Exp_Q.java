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
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.DeadMQueue;

/**
 * Test that a message is delete on a queue when time expired. Use a deadqueue to verify 
 * the message expiration       
 * 
 */
public class Test_Exp_Q extends TestCase {

   
    public static void main(String[] args) {
	new Test_Exp_Q().run();
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
	    MessageConsumer consumer_dead = sessionc.createConsumer(dq);
	   
	    // create a message send to the queue by the pruducer 
	    Message msg = sessionp.createMessage();
	    producer.setTimeToLive(1000L);
	    producer.send(msg);
	    
	    // sleep to be safe that the message expired
	    Thread.sleep(1000L);
	    
	    // the consumer no receive the message from the queue
	    Message msg1= consumer.receive(3000);
	    assertEquals(null,msg1);
	    
	    // but the message is in the deadqueue
	     msg1= consumer_dead.receive(3000);
	     if(msg1 != null){
		 // System.out.println("message expired !");
		 //test messages
		 assertEquals(true, msg1.getBooleanProperty("JMS_JORAM_EXPIRED"));
		 assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
		 assertEquals(msg.getJMSType(),msg1.getJMSType());
		 assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	     }
	     
	    cnx.close();
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

