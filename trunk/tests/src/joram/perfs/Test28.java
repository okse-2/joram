/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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
package joram.perfs;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class MsgList28 implements MessageListener {
    int count = 0;

    long start = 0L;
    long end = 0L;

    public synchronized void waitForEnd(int nb) {
	while (count != nb) {
	    try {
		wait();
	    } catch (InterruptedException exc) {
	    }
	}
    }

    public synchronized void onMessage(Message msg) {
	if (count == 0)
	    start = System.currentTimeMillis();
	int index = -1;
	try {
	    index = msg.getIntProperty("index");
	} catch (JMSException exc) {
	    exc.printStackTrace();
	}
	if (index != count)
	    System.out.println("Waits #" + count + " receives #" + index);
	count += 1;
	end = System.currentTimeMillis();
	notify();
    }
}

/**
 *Test the impact of a durable subscription on the performances of a normal subscription
 */
public class Test28 extends framework.TestCase{
    public static void main (String args[]) throws Exception {
	new Test28().run();
    }
    public void run(){
	Connection cnx = null;
	try {
	    writeIntoFile("======================== start test =====================");
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    boolean durable = Boolean.getBoolean("Durable");

	    Thread.sleep(1000L);

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    Topic topic = Topic.create();
	    topic.setFreeReading();
	    topic.setFreeWriting();

	    User user = User.create("anonymous", "anonymous", 0);

	    ConnectionFactory cf =  LocalConnectionFactory.create();
	    AdminModule.disconnect();

	    cnx = cf.createConnection();
	    cnx.setClientID("Test28");

	    Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons1 = session1.createConsumer(topic);
	    MsgList28 listener = new MsgList28();
	    cons1.setMessageListener(listener);

	    Session session2 = null;
	    MessageConsumer cons2 = null;
	    if (durable) {
		session2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		cons2 = session2.createDurableSubscriber(topic,
							 "dursub","durable=true",
							 false);
	    }
	    Session session3 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = session3.createProducer(topic);

	    cnx.start();

	    for (int i=0; i<10000; i++) {
		Message msg = session3.createMessage();
		if ((i%1000) == 0)
		    msg.setBooleanProperty("durable", true);
		msg.setIntProperty("index", i);
		prod.send(msg);
	    }

	    listener.waitForEnd(10000);
	    //  System.out.println("Durable=" + durable + ", receives " +((10000L * 1000L)/(listener.end - listener.start)) + "msg/s");
	  
	    writeIntoFile("Durable=" + durable + ", receives " +
			       ((10000L * 1000L)/(listener.end - listener.start)) + "msg/s");

	    if (durable) {
		for (int i=0; i<10; i++) {
		    Message msg = cons2.receive(1000L);
		    if (msg == null) {
			System.out.println("receive null message, i=" + i);
		    } else if (msg.getIntProperty("index") != (i*1000)) {
			System.out.println("receive durable message: i=" + i +
					   " != index=" + msg.getIntProperty("index"));
		    }           
		}
		Message msg = cons2.receive(500L);
		if (msg != null)
		    System.out.println("receive non null message, index=" +
				       msg.getIntProperty("index"));
	    }
	    cnx.close();
	} catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    AgentServer.stop();
	    endTest();
	}
    }
}
