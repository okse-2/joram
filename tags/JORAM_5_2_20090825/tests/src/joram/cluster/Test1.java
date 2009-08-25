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
package joram.cluster;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;



/**
 * Test :
 *   
 */
public class Test1 extends TestCase {

   
    public static void main(String[] args) {
	new Test1().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	    startAgentServer((short)1);
	    startAgentServer((short)2);
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    ConnectionFactory cnxF0 = (ConnectionFactory) ictx.lookup("cf0");
	    Topic dest0 = (Topic) ictx.lookup("top0");
	    ConnectionFactory cnxF1 = (ConnectionFactory) ictx.lookup("cf1");
	    Topic dest1 = (Topic) ictx.lookup("top1");
	    ConnectionFactory cnxF2 = (ConnectionFactory) ictx.lookup("cf2");
	    Topic dest2 = (Topic) ictx.lookup("top2");
	    ictx.close();

	    Connection cnx0 = cnxF0.createConnection("publisher00", "publisher00");
	    Session sess0 = cnx0.createSession(true, 0);
	    Session sess0_1 = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer pub = sess0.createProducer(dest0);
	    MessageConsumer sub0 = sess0_1.createConsumer(dest0);
	    
	    Connection cnx1 = cnxF1.createConnection("subscriber10", "subscriber10");
	    Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer sub1 = sess1.createConsumer(dest1);

	    Connection cnx2 = cnxF2.createConnection("subscriber20", "subscriber20");
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer sub2 = sess2.createConsumer(dest2);
	    
	    cnx0.start();
	    cnx1.start();
	    cnx2.start();

	    TextMessage msg = sess0.createTextMessage();

	    int i;
	    for (i = 0; i < 10; i++) {
		msg.setText("Msg " + i);
		pub.send(msg);
	    }
	    sess0.commit();
	    System.out.println(i + " messages published.");
	    
	    for (i = 0; i < 10; i++) {
		msg= (TextMessage)sub0.receive();
		assertEquals("Msg "+i,msg.getText());
	    }
	    
	   
	    System.out.println("ok");
	    
	    for (i = 0; i < 10; i++) {
		msg= (TextMessage)sub1.receive();
		assertEquals("Msg "+i,msg.getText());
	    }
	    System.out.println("ok");

	    for (i = 0; i < 10; i++) {
		msg= (TextMessage)sub2.receive();
		assertEquals("Msg "+i,msg.getText());
	    }
	    System.out.println("ok");
	  
	    cnx0.close();
	    cnx1.close();
	    cnx2.close();
 
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    stopAgentServer((short)1);
	    stopAgentServer((short)2);
	    endTest(); 
	}
    }
    
    
    public void admin() throws Exception {
	// conexion 
	AdminModule.connect("root", "root", 60);
	
	User user00 = User.create("publisher00", "publisher00", 0);
	User user10 = User.create("subscriber10", "subscriber10", 1);
	User user20 = User.create("subscriber20", "subscriber20", 2); 


	javax.jms.ConnectionFactory cf0 =
	    TcpConnectionFactory.create("localhost", 16010);
	javax.jms.ConnectionFactory cf1 =
	    TcpConnectionFactory.create("localhost", 16011);
	javax.jms.ConnectionFactory cf2 =
	    TcpConnectionFactory.create("localhost", 16012);
	
	Topic top0 = (Topic) Topic.create(0);
	Topic top1 = (Topic) Topic.create(1);
	Topic top2 = (Topic) Topic.create(2);
	
	top0.setFreeReading();
	top1.setFreeReading();
	top2.setFreeReading();
	top0.setFreeWriting();
	top1.setFreeWriting();
	top2.setFreeWriting();
	
	top0.addClusteredTopic(top1);
	top0.addClusteredTopic(top2);
	
	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf0", cf0);
	jndiCtx.bind("cf1", cf1);
	jndiCtx.bind("cf2", cf2);
	jndiCtx.bind("top0", top0);
	jndiCtx.bind("top1", top1);
	jndiCtx.bind("top2", top2);
	jndiCtx.close();
    
	AdminModule.disconnect();
    }
}

