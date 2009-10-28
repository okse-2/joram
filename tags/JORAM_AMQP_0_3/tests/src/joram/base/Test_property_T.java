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
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;



/**
 * Test :Test the message properties with topic
 *   
 *     
 */
public class Test_property_T extends TestCase implements javax.jms.MessageListener  {

   
    public static void main(String[] args) {
	new Test_property_T().run();
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
	    MessageConsumer consumer = sessionc.createConsumer(topic);
	   
	    // the consumer records on the topic
	    consumer.setMessageListener(this);

	    Message msg = sessionp.createMessage();
	    msg.setByteProperty("prop_byte",(byte) 42);
	    msg.setShortProperty("prop_short",(short) 1);
	    msg.setIntProperty("prop_int",4);
	    msg.setLongProperty("prop_long",(long) 450);
	    msg.setFloatProperty("prop_float",3.14F);
	    msg.setDoubleProperty("prop_double",(double)88888);
	    msg.setStringProperty("prop_string","string value");
	    

	    producer.send(msg);
	    
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
	    //test messages
	    assertEquals((byte) 42 ,message .getByteProperty("prop_byte"));
	    assertEquals((short) 1 , message.getShortProperty("prop_short"));
	    assertEquals(4 , message.getIntProperty("prop_int"));
	    assertEquals((long) 450 ,message .getLongProperty("prop_long"));
	    assertEquals((float) 3.14 ,message .getFloatProperty("prop_float"));
	    assertEquals((double) 88888 , message.getDoubleProperty("prop_double"));
	    assertEquals("string value" , message.getStringProperty("prop_string"));
	    

	}catch(javax.jms.JMSException JE){
	    JE.printStackTrace( );
	}
    }
}

