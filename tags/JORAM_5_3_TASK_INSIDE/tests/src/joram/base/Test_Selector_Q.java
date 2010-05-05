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
 * Test the message selector with queue
 *    
 */
public class Test_Selector_Q extends TestCase {

     public static void main(String[] args) {
	new Test_Selector_Q().run();
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
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    cnx.start();

	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    // define a selector 
	    MessageConsumer consumer = sessionc.createConsumer(queue,
							       "taille > 180 AND age = 22");
	   
	    MessageConsumer consumer2 = sessionc.createConsumer(queue);
	    // create a message send to the queue by the pruducer 
	    TextMessage msg1 = sessionp.createTextMessage();
	    TextMessage msg2 = sessionp.createTextMessage();
	    TextMessage msg3 = sessionp.createTextMessage();
	    
	    // define properties for the futur selection
	    msg1.setIntProperty("age", 22);
	    msg1.setIntProperty("taille", 182);
	    msg1.setStringProperty("prenom","fabien");
	    msg1.setStringProperty("sexe","M");
	    msg1.setText("message_age_22");
	   	     
	    msg2.setIntProperty("age", 32);
	    msg2.setIntProperty("taille", 170);
	    msg2.setStringProperty("prenom","simon");
	    msg2.setStringProperty("sexe","M");
	    msg2.setText("message_age_32");
	    
	    msg3.setIntProperty("age", 28);
	    msg3.setIntProperty("taille", 181);
	    msg3.setStringProperty("prenom","kara");
	    msg3.setStringProperty("sexe","M");
	    msg3.setText("message_age_28");

	    // send 3 messages 
	    producer.send(msg3);
	    producer.send(msg2);
	    producer.send(msg1);
	  
	    // but one receive,by using selector
	    TextMessage msg=(TextMessage) consumer.receive();
	    assertEquals("message_age_22", msg.getText());
	    assertEquals(22, msg.getIntProperty("age"));
	    assertEquals( 182, msg.getIntProperty("taille"));
	    assertEquals( "fabien", msg.getStringProperty("prenom"));
	    assertEquals( "M",msg.getStringProperty("sexe"));
	   
	    // add timeOut because receive is blocking
	    msg= (TextMessage) consumer.receive(3000);
	    assertTrue(null == msg);

	    // and with a consumer without selector,we find messages 
	    TextMessage msgR=(TextMessage) consumer2.receive();
	    assertTrue(null !=  msgR);
	    msgR=(TextMessage) consumer2.receive();
	    assertTrue(null !=  msgR);
	   
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

