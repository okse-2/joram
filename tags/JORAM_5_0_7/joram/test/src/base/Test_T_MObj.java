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
package base;

import framework.*;

import java.io.*;
import javax.jms.*;
import javax.naming.*;


/**
 * Test :
 *     The object message received by the consumer is the same that 
 *     the object message sent by the producer 
 *     Use a Topic
 */
public class Test_T_MObj extends TestCase implements javax.jms.MessageListener  {

    private ObjectMessage pMsg; // store object  message sent by pruducer
   
    public static void main(String[] args) {
	new Test_T_MObj().run();
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
	    
	    // create an object and a message containing the object send to the topic by the pruducer
	    Obj obj_send=new Obj();
	    ObjectMessage msg = sessionp.createObjectMessage();
	    msg.setObject(obj_send);
	    producer.send(msg);
	    setProducerMessage(msg);

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
	    ObjectMessage msg  = (ObjectMessage) message;
	    ObjectMessage msgP = getProducerMessage();
	    // and extract the object
	    Obj obj_producer=(Obj)msgP.getObject();
	    Obj obj_consumer=(Obj)msg.getObject();
	    //test messages
	    assertEquals(msgP.getJMSMessageID(),msg.getJMSMessageID());
	    assertEquals(msgP.getJMSType(),msg.getJMSType());
	    assertEquals(msgP.getJMSDestination(),msg.getJMSDestination());
	    assertEquals(obj_producer.getA(),obj_consumer.getA());
	}catch(javax.jms.JMSException JE){
	    JE.printStackTrace( );
	}
    }
    public void setProducerMessage(ObjectMessage message){
	pMsg=message;
    }
    public ObjectMessage getProducerMessage(){
	return pMsg;
  }
}

