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
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.DeadMQueue;

import framework.TestCase;

/**
 * Test : set a default dmq and set a default threshold for user
 *    
 */
public class TestDmq5 extends TestCase {

   
    public static void main(String[] args) {
	new TestDmq5().run();
    }
         
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	    
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Topic topic = (Topic) ictx.lookup("topic");
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
	    MessageProducer producer = sessionp.createProducer(topic);
	    MessageConsumer consumerdq = sessionc.createConsumer(dmq);
	    MessageConsumer consumert = sessionc1.createConsumer(topic);
	    // create a text message send to the topic by the pruducer 
	    TextMessage msg = sessionp.createTextMessage();
	    msg= sessionp.createTextMessage("message_1");
	    producer.send(msg);
	  
	   
	    // the consumer receive the message from the dq
	    TextMessage msg1=(TextMessage) consumert.receive();
	    sessionc1.rollback();

	    msg1=(TextMessage) consumert.receive();
	    sessionc1.rollback();

	    msg1=(TextMessage) consumert.receive(2000);
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
     * Admin : Create topic and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
								 "root", "root", 60);
	// create a Topic   
	org.objectweb.joram.client.jms.Topic topic =
	    (org.objectweb.joram.client.jms.Topic) org.objectweb.joram.client.jms.Topic.create("topic"); 
	// set permissions
	topic.setFreeReading();
	topic.setFreeWriting();
	
	DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
	dmq.setFreeReading();
	dmq.setFreeWriting();
	org.objectweb.joram.client.jms.admin.AdminModule.setDefaultDMQ(0,dmq);
	org.objectweb.joram.client.jms.admin.AdminModule.setDefaultThreshold(0,2);


	assertEquals(dmq,org.objectweb.joram.client.jms.admin.AdminModule.getDefaultDMQ());
	assertEquals(2,org.objectweb.joram.client.jms.admin.AdminModule.getDefaultThreshold());

	// create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");


      	javax.jms.ConnectionFactory cf =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("topic", topic);
	jndiCtx.bind("dmq", dmq);
	jndiCtx.close();
	
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}
