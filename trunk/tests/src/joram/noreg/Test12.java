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
import javax.jms.ExceptionListener;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class ExcList12 implements ExceptionListener {
    String name = null;

    ExcList12(String name) {
	this.name = name;
    }

    public void onException(JMSException exc) {
	if (exc instanceof InvalidDestinationException) {
	    System.err.println(name + " InvalidDestinationException OK");
	    Test12.assertTrue(exc instanceof InvalidDestinationException);
	} else {
	   
	    exc.printStackTrace();
	}
    }
}

class MsgList12 implements MessageListener {
    public synchronized void onMessage(Message msg) {
	try {
	    System.out.println("Receive OK");
	} catch (Throwable exc) {
	    exc.printStackTrace();
	}
    }
}

/**
 * check send message on delete queue thrown an exception if there are one server launch
 */
public class Test12 extends BaseTest{
    public static void main (String args[]) throws Exception {
	new Test12().run();
    }
    public void run(){
	try{
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);
	    short sid = Integer.getInteger("sid", 0).shortValue();

	    if (sid != 0)
		joram.framework.TestCase.startAgentServer(sid);

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user = User.create("anonymous", "anonymous", 0);

	    Queue queue = Queue.create(sid);
	    queue.setFreeReading();
	    queue.setFreeWriting();

	    ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 16010);

	    Connection cnx1 = cf.createConnection();
	    cnx1.setExceptionListener(new ExcList12("Receiver"));
	    Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess1.createConsumer(queue);
	    cons.setMessageListener(new MsgList12());
	    cnx1.start();

	    Connection cnx2 = cf.createConnection();
	    cnx2.setExceptionListener(new ExcList12("Sender"));
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess2.createProducer(queue);
	    cnx2.start();

	    Message msg = sess2.createMessage();
	    producer.send(msg);

	    msg = sess2.createMessage();
	    producer.send(msg);

	    queue.delete();

	    msg = sess2.createMessage();
	    try {
		producer.send(msg);
	    } catch (InvalidDestinationException exc) {
		if (sid == 0) {
		    //System.err.println("InvalidDestinationException OK");
		} else {
		    exc.printStackTrace();
		}
	    }

	    Thread.sleep(1000L);

	    msg = sess2.createMessage();
	    try {
		producer.send(msg);
	    } catch (InvalidDestinationException exc) {
		if (sid == 0) {
		    // System.err.println("InvalidDestinationException OK");
		   
		} else {
		    exc.printStackTrace();
		}
	    }

	    cnx1.close();
	    cnx2.close();

	    if (sid != 0)
		joram.framework.TestCase.stopAgentServer(sid);
  
	    AdminModule.disconnect();

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
