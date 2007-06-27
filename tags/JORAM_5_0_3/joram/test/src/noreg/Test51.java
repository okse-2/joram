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
package noreg;

import java.lang.reflect.Method;
import java.io.File;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


/**
 *test user.deleteMessage(), restart of server, and check there is no message
 *
 *
 */


public class Test51 extends framework.TestCase {
  public static void main (String args[])  {
      new Test51().run();
  }
    public void run(){
	try {
	    startAgentServer((short) 0);
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    User user = User.create("anonymous", "anonymous");
	    Topic topic = Topic.create(0, "topic");
	    topic.setFreeReading();
	    topic.setFreeWriting();
	   
	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

	    Connection cnx = cf.createConnection();

	    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess.createDurableSubscriber(topic, "subname");
	    MessageProducer prod = sess.createProducer(topic);
	    cnx.start();

	    cons.close();
	    for (int i=0; i<10; i++) {
		Message msg = sess.createMessage();
		msg.setIntProperty("Index", i);
		prod.send( msg);
	    }

	    sess.close();
	    cnx.close();


	    Subscription sub = user.getSubscription("subname");
	    //System.out.println("before deletion -> " + sub.getMessageCount() +" should be 10");
	    assertEquals(10,sub.getMessageCount());
	    String ids[] = user.getMessageIds("subname");
	    for (int i=0; i<ids.length; i++) {
		user.deleteMessage("subname", ids[i]);
	    }
	    sub = user.getSubscription("subname");
	    // System.out.println("after deletion -> " + sub.getMessageCount() +" should be 0");
	    assertEquals(0,sub.getMessageCount());
	   
	    AdminModule.disconnect();

	    Thread.sleep(1000L);
	    System.out.println("Stop Server#0");
	    stopAgentServer((short) 0);
	    Thread.sleep(1000L);
	    System.out.println("Start Server#0");
	    startAgentServer((short) 0);
	    Thread.sleep(1000L);

	    AdminModule.connect("root", "root", 60);
	    topic = Topic.create(0, "topic");
	    user = User.create("anonymous", "anonymous");
	    sub = user.getSubscription("subname");
	    // System.out.println("after restart -> " + sub.getMessageCount() +" should be 0");
	    assertEquals(0,sub.getMessageCount());
	    AdminModule.disconnect();

	    Thread.sleep(1000L);
	   
	} catch (Exception exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	     System.out.println("Stop Server#0");
	    stopAgentServer((short) 0);
	    endTest();
	}

    }
}

