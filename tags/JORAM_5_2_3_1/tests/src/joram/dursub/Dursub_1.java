/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
package joram.dursub;

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
 * Test the fonctioning of DurableSubscriber. Create a dursub. close dursub. send message.
 * restart dursub and receive message
 *     Using a Topic
 */
public class Dursub_1 extends TestCase  {
   
      public static void main(String[] args) {
	new Dursub_1().run();
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
	    ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
	    ictx.close();
	    
	    Connection cnx = cf.createConnection();
	    // connection for subscriber
	    Connection cnx1 = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						 Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx1.createSession(false,
						  Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	    cnx1.start();
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(topic);
	    // subscribe 
	    TopicSubscriber sub = sessionc.createDurableSubscriber(topic,"topic");  
	    
	    // close connection of subscriber
	    cnx1.close();
	    // send a message
	    TextMessage msg = sessionp.createTextMessage();
	    msg.setText("message_text");
	    producer.send(msg);
	    
	    // reconnection of subscribe
	    cnx1 = cf.createConnection();
	    sessionc = cnx1.createSession(false,Session.AUTO_ACKNOWLEDGE);
	    sub = sessionc.createDurableSubscriber(topic,"topic");
	    cnx1.start();
	    // and receive the message
	    TextMessage m = (TextMessage)sub.receive();
	    
	    assertEquals("message_text",m.getText());
	    cnx.close();
	    cnx1.close();
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

	javax.jms.ConnectionFactory cf1 =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);
	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("cf1", cf1);
	jndiCtx.bind("topic", topic);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
   
}

