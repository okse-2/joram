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
package joram.dmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.DeadMQueue;

import framework.TestCase;

/**
 * Test : set a default dmq  and a default threshold
 *    
 */
public class TestDmq4 extends TestCase {

   
    public static void main(String[] args) {
	new TestDmq4().run();
    }
         
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	    
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Queue queue = (Queue) ictx.lookup("queue");
	    DeadMQueue dmq =(DeadMQueue) ictx.lookup("dmq");
   	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();

	    Connection cnx = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc1 = cnx.createSession(true,
						Session.AUTO_ACKNOWLEDGE);
	   
	    cnx.start();

	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    MessageConsumer consumerdq = sessionc.createConsumer(dmq);
	    MessageConsumer consumerq = sessionc1.createConsumer(queue);
	    // create a text message send to the queue by the pruducer 
	    TextMessage msg = sessionp.createTextMessage();
	    msg= sessionp.createTextMessage("message_1");
	    producer.send(msg);
	  
	   
	    // the consumer receive the message from the dq
	    TextMessage msg1=(TextMessage) consumerq.receive();
	    sessionc1.rollback();

	    msg1=(TextMessage) consumerq.receive();
	    sessionc1.rollback();

	    msg1=(TextMessage) consumerq.receive(2000);
	    assertEquals(null,msg1);
	   

	    msg1=(TextMessage) consumerdq.receive();
	    //test messages
	    assertEquals("message_1",msg1.getText());
	    assertEquals(3, msg1.getIntProperty("JMSXDeliveryCount"));
	    
	    cnx.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
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
	    (org.objectweb.joram.client.jms.Queue) org.objectweb.joram.client.jms.Queue.create("queue"); 
	// set permissions
	queue.setFreeReading();
	queue.setFreeWriting();
	
	DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
	dmq.setFreeReading();
	dmq.setFreeWriting();
	org.objectweb.joram.client.jms.admin.AdminModule.setDefaultDMQ(0,dmq);
	org.objectweb.joram.client.jms.admin.AdminModule.setDefaultThreshold(0,2);

	// create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");


      	javax.jms.ConnectionFactory cf =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("queue", queue);
	jndiCtx.bind("dmq", dmq);
	jndiCtx.close();
	
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}
