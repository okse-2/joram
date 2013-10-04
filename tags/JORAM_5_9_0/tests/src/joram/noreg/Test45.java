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
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test timeToLive with 3 messages. send 2 message and wait. send a third and receive. 
 * receive only the third message, ttl expire for other. 
 *
 */
public class Test45 extends BaseTest {
  public static void main (String args[])  {
      new Test45().run();
  }
    public void run(){
	try {
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    Topic topic = Topic.create("Topic");
	    topic.setFreeReading();
	    topic.setFreeWriting();
	    User user = User.create("anonymous", "anonymous", 0);
	    AdminModule.disconnect();

	    ConnectionFactory cf =  LocalConnectionFactory.create();

	    Connection cnx = cf.createConnection();
	    cnx.setClientID("Test45");
	    cnx.start();

	    Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess1.createDurableSubscriber(topic, "subname");
	    sess1.close();

	    Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess2.createProducer(topic);
	    producer.setTimeToLive(10000L);
	    Message msg = sess2.createMessage();
	    msg.setStringProperty("name", "msg#1");
	    producer.send(msg);
	    Thread.sleep(5000L);

	    msg.setStringProperty("name", "msg#2");
	    producer.send(msg);
	    Thread.sleep(10000L);

	    msg.setStringProperty("name", "msg#3");
	    producer.send(msg);
	    Thread.sleep(5000L);

	    sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    cons = sess1.createDurableSubscriber(topic, "subname");
	    msg = cons.receive();
	    // System.out.println("receives: " + msg.getStringProperty("name") +" should be msg#3");
	    assertEquals("msg#3", msg.getStringProperty("name"));
	    sess1.close();

	    sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    sess1.unsubscribe("subname");
	    sess1.close();

	    sess2.close();
	    cnx.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}


    }
}
