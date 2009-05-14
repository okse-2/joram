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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;



/**
 * Test Client_ackowledge. If no acknowledgement, the message is sent again.
 * When use manual ackowledge the message is deleting
 * Use a topic, so use subcriberdurable.
 *
 */
public class Test_CAck1_T extends TestCase  {
   
    public static void main(String[] args) {
	new Test_CAck1_T().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Topic topic = (Topic) ictx.lookup("topic");
   	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();

	    Connection cnx = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						Session.CLIENT_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						Session.CLIENT_ACKNOWLEDGE);
	    cnx.start();
	    
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(topic);
	    TopicSubscriber consumer = sessionc.createDurableSubscriber(topic,"topic");  
	   	  
	    Message msg = sessionp.createMessage();
	    producer.send(msg); 
	  
	    Message msg1 = consumer.receive(3000);
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
 	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	    //there is no more message
	    msg1= consumer.receive(3000);
	    assertEquals(null,msg1);
	   
	    // session close without acknoledge
	    sessionc.close();

	    // reconnection
	    sessionc = cnx.createSession(false,Session.CLIENT_ACKNOWLEDGE);
	    consumer = sessionc.createDurableSubscriber(topic,"topic"); 
	    // message is redelivred
	    msg1= consumer.receive(3000);
	    //test
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	  
	    // acknowledge
	    msg1.acknowledge();
	   
	    sessionc.close();
     	    sessionc = cnx.createSession(false,Session.CLIENT_ACKNOWLEDGE);
	    consumer = sessionc.createDurableSubscriber(topic,"topic"); 
	    // message is not redelivred 
	    msg1= consumer.receive(3000);
	    assertEquals(null,msg1);
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

        // create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
	// set permissions
	topic.setFreeReading();
	topic.setFreeWriting();

      	javax.jms.ConnectionFactory cf =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("topic", topic);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
 
}

