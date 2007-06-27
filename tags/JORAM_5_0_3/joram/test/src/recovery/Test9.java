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
package recovery;

import java.lang.reflect.Method;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

/**
 * Test recovery : start 2 server. queue on server 1. User attach on server 0. 
 * kill server 1. send message. restart server 1. and receivewith the same user 
 * use some message
 */
public class Test9 extends framework.TestCase {
    static ConnectionFactory cf;
    static Destination dest;
    static Connection cnx1;

    static int MsgSize = 1024;

    public static void main (String args[]) throws Exception {
	new Test9().run();
    }
    public void run(){
	try{
	    startAgentServer((short) 1);

	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();
	   
	    Thread.sleep(10000L);

	    AdminModule.collocatedConnect("root", "root");

	    User user = User.create("anonymous", "anonymous", 0);
	    dest = Queue.create(1);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    cf = new LocalConnectionFactory();
	    AdminModule.disconnect();

	    stopAgentServer((short) 1);

	    Connection cnx = cf.createConnection();
	    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = session.createProducer(dest);
	    cnx.start();

	    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

	    byte[] content = new byte[MsgSize];
	    for (int i = 0; i< MsgSize; i++)
		content[i] = (byte) (i & 0xFF);

	    for (int i=0; i<10; i++) {
		for (int j=0; j<50; j++) {
		    BytesMessage msg = session.createBytesMessage();
		    msg.setIntProperty("index", j);
		    msg.writeBytes(content);
		    producer.send(msg);
		}
		//System.out.println("step#" + i);
		// Thread.sleep(1000L);
	    }
	    System.out.println("step");
	    session.close();
	    cnx.close();
	   
	    startAgentServer((short) 1);
	    // wait because server 0 send all message to server 1
	    // and receive request is buffering at end of send message 
	    Thread.sleep(40000L);
	    
	    cnx = cf.createConnection();
	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = session.createConsumer(dest);
	    cnx.start();
	     System.out.println("step 1");
	    BytesMessage msg =null;
	    for (int i=0; i<10; i++) {
		for (int j=0; j<50; j++) {
		    msg =(BytesMessage) cons.receive();
		    assertEquals(j, msg.getIntProperty("index"));
		}
		System.out.println("step#" + i);
		//	Thread.sleep(1000L);
	    }

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    System.out.println("fin#");
	    stopAgentServer((short) 1);
	    AgentServer.stop();
	    endTest();
	}
    }
}
