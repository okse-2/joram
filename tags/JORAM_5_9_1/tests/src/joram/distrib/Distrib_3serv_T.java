/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s):  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.distrib;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;


/**
 * Test distributed architecture. Use 3 servers. The producer is attached to server 0. 
 * The consumer is attached to server 2. The topic is created on server 1.
 *
 */

public class Distrib_3serv_T extends TestCase {
       
    public static void main(String[] args) {
	new Distrib_3serv_T().run();
    }
          
    public void run() {
	try {
	    System.out.println("servers start");
	    startAgentServer((short)0);
	    startAgentServer((short)1);
	    startAgentServer((short)2);

	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Topic topic = (Topic) ictx.lookup("topic");
	    ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
	    ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
	    ictx.close();


	    Connection cnx = cf0.createConnection();
	    Connection cnxCons = cf1.createConnection();
	    cnxCons.setClientID("Distrib_3serv_T");
	    Session sessionp = cnx.createSession(false,
					 Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnxCons.createSession(false,
					 Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	    cnxCons.start();

	    TopicSubscriber consumer = sessionc.createDurableSubscriber(topic,"top");
	    MessageProducer producer = sessionp.createProducer(topic);

	   	    
	    TextMessage msg = null; 
	    TextMessage msg1= null;
	    
	    for(int j=0;j<10;j++){
		msg = sessionp.createTextMessage();
		msg.setText("messagedist#"+j);
		producer.send(msg);
	    }
	   	   
	    // the consumer receive the message from the topic
	  
	    for(int j=0;j<10;j++){
		msg1 = (TextMessage) consumer.receive();
		//	System.out.println(msg1.getText());
		assertTrue(msg1.getText().startsWith("messagedist#"));
	    }
	    
	    msg1 = (TextMessage) consumer.receive(3000);
	    assertEquals(null,msg1);   
	    cnx.close();
	    cnxCons.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}
	finally {
	    System.out.println("Servers stop ");
	    stopAgentServer((short)0);
	    stopAgentServer((short)1);
	    stopAgentServer((short)2);
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
	    (org.objectweb.joram.client.jms.Topic) org.objectweb.joram.client.jms.Topic.create(1); 

        // create a user
	org.objectweb.joram.client.jms.admin.User user0 =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous",0);
	org.objectweb.joram.client.jms.admin.User user1 =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous",2);
	// set permissions
	topic.setFreeReading();
	topic.setFreeWriting();

      	javax.jms.ConnectionFactory cf0 =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);
	javax.jms.ConnectionFactory cf1 =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2562);


	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf0", cf0);
	jndiCtx.bind("cf1", cf1);
	jndiCtx.bind("topic", topic);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}

