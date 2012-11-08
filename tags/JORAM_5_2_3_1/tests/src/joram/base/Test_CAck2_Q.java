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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;



/**
 * Test Client_ackowledge. send 3 message,if acknowledge the second, the message 1 
 * and message 2 are acknowledged, not the message 3, so it is sent again when reconnection
 * Use a queue 
 *
 */
public class Test_CAck2_Q extends TestCase {

   
    public static void main(String[] args) {
	new Test_CAck2_Q().run();
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

	  
	    Session sessionp = cnx.createSession(false,
						Session.CLIENT_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.CLIENT_ACKNOWLEDGE);
	   
	    cnx.start();
	  
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    MessageConsumer consumer = sessionc.createConsumer(queue);
	   
	    // create  messages send to the queue by the pruducer 
	    TextMessage msg1 = sessionp.createTextMessage();
	    TextMessage msg2 = sessionp.createTextMessage();
	    TextMessage msg3 = sessionp.createTextMessage();
	    msg1.setText("message_1");
	    msg2.setText("message_2");
	    msg3.setText("message_3");

	    producer.send(msg1);
	    producer.send(msg2);
	    producer.send(msg3);
	    
	    // receive the message
	    TextMessage msgr1 =( TextMessage) consumer.receive(3000);
	    TextMessage msgr2 =( TextMessage) consumer.receive(3000);
	 	   
	    assertEquals("message_1",msgr1.getText());
	    assertEquals("message_2",msgr2.getText());
	   
	    msgr2.acknowledge();
	    TextMessage msgr3 =( TextMessage) consumer.receive(3000);
	    assertEquals("message_3",msgr3.getText());
	   
	    // session close without acknoledge
	    sessionc.close();
	    
	    // reconnection
	    sessionc = cnx.createSession(false,
					 Session.CLIENT_ACKNOWLEDGE);
	    consumer = sessionc.createConsumer(queue);
	    // message 3 is redelivred
	    msgr3=( TextMessage) consumer.receive(3000);
	    //test
	    assertEquals("message_3",msgr3.getText());
	   
	    msgr1= ( TextMessage)consumer.receive(3000);
	    assertEquals(null,msgr1);
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

