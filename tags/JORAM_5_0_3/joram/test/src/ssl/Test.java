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
package ssl;

import framework.*;

import java.io.*;
import javax.jms.*;
import javax.naming.*;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.tcp.*;
/**
 * Test :
 *     
 */
public class Test extends TestCase {

   
    public static void main(String[] args) {
	new Test().run();
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
	    // create a text message send to the queue by the pruducer 
	    TextMessage msg = sessionp.createTextMessage();
	    msg.setText("message_type_text");
	    producer.send(msg);
	    // the consumer receive the message from the queue
	    Message msg1= consumer.receive();
	    TextMessage msg2=(TextMessage) msg1;
	    
	    //test messages
	    assertEquals(msg.getJMSMessageID(),msg1.getJMSMessageID());
	    assertEquals(msg.getJMSType(),msg1.getJMSType());
	    assertEquals(msg.getJMSDestination(),msg1.getJMSDestination());
	    assertEquals("message_type_text",msg2.getText());
	    
	    cnx.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    System.out.println("Server stop ");
	    killAgentServer((short)0);
	    endTest(); 
	}
    }
    
    
    public void admin() throws Exception {

	AdminModule.connect("root", "root", 60, "org.objectweb.joram.client.jms.tcp.ReliableSSLTcpClient");

	Queue queue = (Queue) Queue.create("queue");
	Topic topic = (Topic) Topic.create("topic");
    
	User user = User.create("anonymous", "anonymous");

	queue.setFreeReading();
	topic.setFreeReading();
	queue.setFreeWriting();
	topic.setFreeWriting();

	javax.jms.ConnectionFactory cf =
	    TcpConnectionFactory.create("localhost", 
					16010, 
					"org.objectweb.joram.client.jms.tcp.ReliableSSLTcpClient");
	javax.jms.QueueConnectionFactory qcf =
	    QueueTcpConnectionFactory.create("localhost", 
					     16010, 
					     "org.objectweb.joram.client.jms.tcp.ReliableSSLTcpClient");
	javax.jms.TopicConnectionFactory tcf =
	    TopicTcpConnectionFactory.create("localhost", 
					     16010, 
					     "org.objectweb.joram.client.jms.tcp.ReliableSSLTcpClient");

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("qcf", qcf);
	jndiCtx.bind("tcf", tcf);
	jndiCtx.bind("queue", queue);
	jndiCtx.bind("topic", topic);
	jndiCtx.close();

	AdminModule.disconnect();


    }
}

