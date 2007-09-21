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

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * test recovery with 2 server and a durablesubscriber. start 2 server. send message.
 * stop first server. send 2 message. restart 2 server and check receive four message
 *
 */
public class Test6 extends framework.TestCase {
  public static void main (String args[]) {
      new Test6().run();
  }
    public void run(){
	try {
	    startAgentServer((short) 1);
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    Topic topic = Topic.create(1, "Topic");
	    topic.setFreeReading();
	    topic.setFreeWriting();
	    User user0 = User.create("anonymous", "anonymous", 0);
	    User user1 = User.create("anonymous", "anonymous", 1);
	    AdminModule.disconnect();

	    ConnectionFactory cf0 =  LocalConnectionFactory.create();
	    ConnectionFactory cf1 =  TcpConnectionFactory.create("localhost", 16011);

	    Connection cnx = cf1.createConnection();
	    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer consumer = session.createDurableSubscriber(topic, "subname");
	    session.close();
	    cnx.close();

	    //System.out.println("Subscription done");

	    cnx = cf0.createConnection();
	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = session.createProducer(topic);
	    Message msg = session.createMessage();
	    msg.setStringProperty("name", "msg#1");
	    producer.send(msg);
	    msg.setStringProperty("name", "msg#2");
	    producer.send(msg);

	    //System.out.println("Messages #1, #2 sent");

	    Thread.sleep(1000L);
	    stopAgentServer((short) 1);

	    System.out.println("Server#1 stopped");

	    msg.setStringProperty("name", "msg#3");
	    producer.send(msg);
	    msg.setStringProperty("name", "msg#4");
	    producer.send(msg);
	    session.close();
	    cnx.close();

	    //System.out.println("Messages #3, #4 sent");

	    Thread.sleep(1000L);
	    AgentServer.stop();
	    AgentServer.reset();
	    Thread.sleep(1000L);

	    System.out.println("Server#0 stopped");

	    startAgentServer((short) 1);

	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    Thread.sleep(1000L);

	    System.out.println("Servers #0, #1 started");

	    cnx = cf1.createConnection();
	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    consumer = session.createDurableSubscriber(topic, "subname");
	    cnx.start();

	    //System.out.println("before receive");
	    msg = consumer.receive();
	    
	    //System.out.println("receives: " + msg.getStringProperty("name"));
	    assertEquals("msg#1", msg.getStringProperty("name"));
	    msg = consumer.receive();
	    
	    //System.out.println("receives: " + msg.getStringProperty("name"));
	    assertEquals("msg#2", msg.getStringProperty("name"));
	    msg = consumer.receive();
	    
	    //System.out.println("receives: " + msg.getStringProperty("name"));
	    assertEquals("msg#3", msg.getStringProperty("name"));
	    msg = consumer.receive();
	    
	    //System.out.println("receives: " + msg.getStringProperty("name"));
	    assertEquals("msg#4", msg.getStringProperty("name"));
	    session.close();


	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    session.unsubscribe("subname");
	    session.close();
	    cnx.close();

	    Thread.sleep(1000L);
	 
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    stopAgentServer((short) 1);
	    AgentServer.stop(); 
	    endTest();
	}

	
    }
}
