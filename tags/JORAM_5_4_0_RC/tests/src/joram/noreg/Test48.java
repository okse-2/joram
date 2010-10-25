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
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


/**
 * test queue.clear() , restart of server, and check there is no message
 *
 */

public class Test48 extends BaseTest {
    public static void main (String args[])  {
	new Test48().run();
    }
    public void run(){
	try {
	    framework.TestCase.startAgentServer((short) 0);
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    User user = User.create("anonymous", "anonymous");
	    Queue queue = Queue.create(0, "queue");
	    queue.setFreeReading();
	    queue.setFreeWriting();
	    //       AdminModule.disconnect();
	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

	    Connection cnx = cf.createConnection();

	    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = sess.createProducer(queue);
	    cnx.start();

	    for (int i=0; i<10; i++) {
		Message msg = sess.createMessage();
		msg.setIntProperty("Index", i);
		prod.send( msg);
	    }

	    sess.close();
	    cnx.close();

	    //System.out.println("before clear -> " + queue.getPendingMessages() + " should be 10");
	    assertEquals(10,queue.getPendingMessages());
	    queue.clear();
	    //System.out.println("after clear -> " + queue.getPendingMessages() +" should be 0");
	    assertEquals(0,queue.getPendingMessages());
	   
	    AdminModule.disconnect();

	    Thread.sleep(1000L);
	    System.out.println("Stop Server#0");
	    framework.TestCase.stopAgentServer((short) 0);
	    Thread.sleep(1000L);
	    System.out.println("Start Server#0");
	    framework.TestCase.startAgentServer((short) 0);
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    queue = Queue.create(0, "queue");
	    // System.out.println("after restart -> " + queue.getPendingMessages() +" should be 0");
	    assertEquals(0,queue.getPendingMessages());
	    
	    AdminModule.disconnect();

	    Thread.sleep(1000L);
	   
	} catch (Exception exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	     System.out.println("Stop Server#0");
	    framework.TestCase.stopAgentServer((short) 0);
	    endTest();
	}
    }
}
