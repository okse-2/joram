/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent D.T.
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package recovery;

import framework.*;

import java.io.*;
import javax.jms.*;
import javax.naming.*;


/**
 * Test : send message to the server; kill the server; restart the server. 
 * The server restores messages, and then the receiver can receive it.
 * Use a Topic and durablSubscriber
 *
 */
public class Recover_3 extends TestCase {

    private Connection cnx;
    private Connection cnxCons;
    private Session sessionp ;
    private Session sessionc ; 
    private ConnectionFactory cf;
    private ConnectionFactory cfCons;
    private MessageProducer producer;
    private TopicSubscriber consumer;
    private Topic topic;
   

    public void connect(){
	try{
	    cnx = cf.createConnection();
	    cnxCons = cf.createConnection();
	    sessionp = cnx.createSession(false,
					 Session.AUTO_ACKNOWLEDGE);
	    sessionc = cnxCons.createSession(false,
					 Session.AUTO_ACKNOWLEDGE);
	    cnx.start();
	    cnxCons.start();
	    consumer = sessionc.createDurableSubscriber(topic,"topic");
	    producer = sessionp.createProducer(topic);
	}catch(Exception exc){
	    exc.printStackTrace();
	    error(exc);
	}
    }
    
    public static void main(String[] args) {
	new Recover_3().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	    
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    topic = (Topic) ictx.lookup("topic");
   	    cf = (ConnectionFactory) ictx.lookup("cf");
	    ictx.close();
	    connect();
	   	    
	    TextMessage msg = null; 

	    
	    for(int j=0;j<10;j++){
		msg = sessionp.createTextMessage();
		msg.setText("message#"+j);
		producer.send(msg);
	    }
	    System.out.println("message sent");
	    // kill and restart server
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
       	    System.out.println("server start");
	    startAgentServer((short)0);

	    connect();
	    // the consumer receive the message from the topic
	    TextMessage msg1= null;
	    for(int j=0;j<10;j++){
		msg1 = (TextMessage) consumer.receive();
		System.out.println(msg1.getText());
		assertTrue(msg1.getText().startsWith("message#"));
	    }
	    	   
	    cnx.close();
	    cnxCons.close();
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

