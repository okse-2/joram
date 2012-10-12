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
package joram.withbug;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

class MsgList53 implements MessageListener {
  public void onMessage(Message msg) {
    try {
      int index = msg.getIntProperty("Index");
      if (index <= 2) {
        System.out.println("receives msg#" + index);
        //         msg.acknowledge();
      } else {
        System.out.println("should not receives msg#" + index + ", ignore");
      }
      if (index == 2) {
        synchronized (Test1.lock) {
          System.out.println("notify");
          Test1.lock.notify();
        }
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

public class Test1 extends BaseTestCase {
  static Object lock = null;

  public static void main (String args[])  {
      new Test1().run();
  }
    public void run(){
	try {
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    Thread.sleep(1000L);
	    
	    lock = new Object();
	    
	    AdminModule.connect("root", "root", 60);
	    User user = User.create("anonymous", "anonymous");
	    Topic topic = Topic.create(0);
	    topic.setFreeReading();
	    topic.setFreeWriting();

	    ConnectionFactory cf =  LocalConnectionFactory.create();
	    
	    Connection cnx = cf.createConnection();
	    
	    Session sess1 = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod1 = sess1.createProducer(topic);
	    
	    Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons2 = sess2.createDurableSubscriber(topic, "subname");
	    cons2.setMessageListener(new MsgList53());
	    
	    cnx.start();
	    
	    synchronized(lock) {
		
		for (int i=0; i<50; i++) {
		    Message msg = sess1.createMessage();
		    msg.setIntProperty("Index", i);
		    prod1.send(msg);
		}

		sess1.commit();
		System.out.println("send msgs");
		sess1.close();
		
        // synchronized(lock) {
		lock.wait();
        System.out.println("before close");
		cons2.close();
		sess2.close();
		System.out.println("after close");
	    }
	    Thread.sleep(1000L);
	    cnx.close();
	    System.out.println("cnx close");
	    
	    Subscription sub = user.getSubscription("subname");
	    System.out.println("after close -> " + sub.getMessageCount() +
			       " should be 47");
	    
	    AdminModule.disconnect();

	    Thread.sleep(1000L);
	   
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    System.out.println("server stop");
	    fr.dyade.aaa.agent.AgentServer.stop();
	    endTest();
	}
    }
}
