/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 *
 */
public class Test56 extends BaseTest{
    public static void main (String args[]) {
	new Test56().run();
    }
    public void run(){
	try {
	    Connection cnx1 = null;
	    Connection cnx2 = null;
	    
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();
	    
	    Thread.sleep(1000L);
	    
	    AdminModule.connect("localhost", 16010, "root", "root", 60);
	    Queue dest = Queue.create(0);
	    dest.setFreeReading();
	    dest.setFreeWriting();
	    User user = User.create("anonymous", "anonymous", 0);
	    AdminModule.disconnect();
	    
	    ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 16010);
	    cnx1 = cf.createConnection();
	    cnx2 = cf.createConnection();
	    
	    Session session1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    javax.jms.Queue q1 = session1.createQueue(dest.getQueueName());
	    MessageProducer prod = session1.createProducer(q1);
	    
	    Session session2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    javax.jms.Queue q2 = session2.createQueue(dest.getQueueName());
	    MessageConsumer cons = session2.createConsumer(q2);
	    
	    cnx1.start();
	    cnx2.start();
	    
	    TextMessage message = session1.createTextMessage();
	    message.setText("This is message");
	    System.out.println("Sending message: " + message.getText());
	    prod.send(message);
	    System.out.println("message sent");
	    
	    message = (TextMessage) cons.receive();
	    System.out.println("Receiving message: " + message.getText());
	    assertEquals("This is message", message.getText());
	    cnx1.close();
	    cnx2.close();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally {
	    AgentServer.stop();
	    endTest();
	}
	
    }
}
