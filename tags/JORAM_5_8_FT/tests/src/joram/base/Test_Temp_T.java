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
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;




/**
 *  Test the functioning of Temporary Topic
 *    
 */
public class Test_Temp_T extends TestCase implements javax.jms.MessageListener  {

    
    public static void main(String[] args) {
	new Test_Temp_T().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();
	    
	    Connection cnx = cf.createConnection();
	    Session sessionp = cnx.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
	    Session sessionc = cnx.createSession(false,
						 Session.AUTO_ACKNOWLEDGE);
	  
	    // temporary topic is creating by a session 
	    TemporaryTopic temptopic = sessionp.createTemporaryTopic();
	   
	    cnx.start();
	  
	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(temptopic);
	    MessageConsumer consumer = sessionc.createConsumer(temptopic); 
	   
	    // the consumer records on the topic
	    consumer.setMessageListener(this);
	    TextMessage msg = sessionp.createTextMessage();
	    msg.setText("test_temporary_topic");
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
     * Admin : Create a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
								 "root", "root", 60);

	// create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
	
	javax.jms.ConnectionFactory cf =
	    org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

	 
	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }


    public void onMessage(Message message) {
	System.out.println("message receive");
	try{
	    TextMessage msg = (TextMessage) message;
	    //test messages
	    assertEquals("test_temporary_topic",msg.getText());
	}catch(javax.jms.JMSException JE){
	    JE.printStackTrace( );
	}
    }
}

