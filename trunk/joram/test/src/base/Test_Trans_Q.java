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

/**
 * Test transacted. 
 * Use a queue 
 *
 */
public class Test_Trans_Q extends TestCase {
    
    public static void main(String[] args) {
	new Test_Trans_Q().run();
    }
    
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	    	    
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Queue queue = (Queue) ictx.lookup("queue");
	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();
	    
	    Connection cnx = cf.createConnection();
	    Session sessionp = cnx.createSession(true,
						 Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(true,
						 Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	    
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    MessageConsumer consumer = sessionc.createConsumer(queue);
	    
	    // create a message send to the queue by the pruducer 
	    Message msg = sessionp.createMessage();
	    
	    producer.send(msg);
	    
	    Message msg1= consumer.receive(3000);
	    //sessionp not commit, so the message is not delivred
	    assertEquals(null,msg1); 
	    
	    // commit
	    sessionp.commit();
	    
	    // receive the message
	    msg1= consumer.receive(3000);
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	    
	    // session close without commit receive message
	    sessionc.close();
	      
	    // reconnection
	    sessionc = cnx.createSession(true,
					 Session.AUTO_ACKNOWLEDGE);
	    consumer = sessionc.createConsumer(queue);
	    // message is redelivred
	    msg1= consumer.receive(3000);
	    //test
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	    
	    sessionc.commit();
	    
	    sessionc.close();
     	    sessionc = cnx.createSession(true,
					 Session.AUTO_ACKNOWLEDGE);
	    consumer = sessionc.createConsumer(queue);
	    // message is not redelivred 
	    msg1= consumer.receive(3000);
	    assertEquals(null,msg1);
	    cnx.close();
 
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
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}

