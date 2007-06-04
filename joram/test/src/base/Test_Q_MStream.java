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
 *     The Stream message received by the consumer is the same that 
 *     the Stream message sent by the producer 
 *     Use a Queue
 */
public class Test_Q_MStream extends TestCase {

    public static void main(String[] args) {
	new Test_Q_MStream().run();
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
	    MessageConsumer consumer = sessionc.createConsumer(queue);
	   
	    // create a Stream message send to the queue by the pruducer 
	    StreamMessage msg = sessionp.createStreamMessage();
	   
	    byte[] content = new byte[10];
	    for (int i = 0; i< 10 ; i++)
		content[i] = (byte) i ; 
	     msg.writeBytes(content);
	     msg.writeByte((byte )15);
	     msg.writeBoolean(true);
	     
	     Character ch='e';
	     msg.writeChar(ch);
	     msg.writeString("it is a string");
	     
	     producer.send(msg);
	   
	   
	    // the consumer receive the message from the queue
	    Message msg1= consumer.receive();
	    StreamMessage msg2=(StreamMessage) msg1;
	   
	    //read in same order that send
	    byte[] receive = new byte[10];
	    int nbread = msg2.readBytes(receive);
	    byte rbyte=msg2.readByte();
	    // bool to string -> Convertion possible with StreamMessge (see javadoc)
	    String rbool=msg2.readString();
	    Character rch=msg2.readChar();
	    String rst=msg2.readString();
	    
	    //test messages
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	    assertEquals(content,receive,10);
	    assertEquals("it is a string",rst);
	    assertEquals('e',rch.charValue());
	    assertEquals("true",rbool);
	    assertEquals((byte)15,rbyte);
	   
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

