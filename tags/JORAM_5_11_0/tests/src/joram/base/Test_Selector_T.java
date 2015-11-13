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
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;



/**
 * Test the selector
 *     Use a Topic
 */
public class Test_Selector_T extends TestCase implements javax.jms.MessageListener  {
    
    
    public static void main(String[] args) {
	new Test_Selector_T().run();
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
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						 Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	    
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(topic);
	    MessageConsumer consumer = sessionc.createConsumer(topic,"prenom LIKE 'o%'");
	    
	    // the consumer records on the topic
	    consumer.setMessageListener(this);
	    
	    TextMessage msg1 = sessionp.createTextMessage();
	    TextMessage msg2 = sessionp.createTextMessage();
	    TextMessage msg3 = sessionp.createTextMessage();
	    //consumer.setMessageListener(this);
	    
	    // define properties for the futur selection
	    msg1.setIntProperty("age", 30);
	    msg1.setIntProperty("taille", 182);
	    msg1.setStringProperty("prenom","odil");
	    msg1.setStringProperty("sexe","F");
	    msg1.setText("message_age_30");
	    
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


    public void onMessage(Message message) {
	System.out.println("message receive");
	try{
	    //test message
	    TextMessage msg  = (TextMessage) message;
	    assertEquals("message_age_30", msg.getText());
	    assertEquals(30, msg.getIntProperty("age"));
	   assertEquals( 182, msg.getIntProperty("taille"));
	  assertEquals( "odil", msg.getStringProperty("prenom"));
	   assertEquals( "F",msg.getStringProperty("sexe"));
	}catch(javax.jms.JMSException JE){
	    JE.printStackTrace( );
	}
    }
}

