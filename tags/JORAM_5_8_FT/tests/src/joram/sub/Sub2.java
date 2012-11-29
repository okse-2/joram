/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Tachker Nicolas (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.sub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;


/**
 * Test the handling of errors with topic subscription:
 *  - subscription to a deleted topic on same server.
 *  - subscription to a deleted topic on different server.
 *  - subscription to a bad topic (inexistent server).
 * A JMS exception must be throw in each case.
 * 
 * @see     Joram/JORAM-14
 */
public class Sub2 extends TestCase {
  
  public static void main(String[] args) {
    new Sub2().run();
  }

  Topic topic0, topic1, topic2;
  ConnectionFactory cf;
  Connection cnx;
  Session session;
  
  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);
      startAgentServer((short) 1);

      cf = TcpConnectionFactory.create("localhost", 2560);

      AdminModule.connect(cf);

      // create topics
      topic0 = Topic.create(0, "topic0");
      topic1 = Topic.create(1, "topic1");

      // create a user
      User.create("anonymous", "anonymous");

      // delete topics
      topic0.delete();
      topic1.delete();

      AdminModule.disconnect();

      System.out.println("admin config ok");

      // connection for subscriber
      cnx = cf.createConnection();
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      Thread subThread = new Thread(new Runnable() {
        public void run() {
          // Try to subscribe to a local inexistent topic.
          try {
            System.out.println("Try to subscribe: " + topic0);
            MessageConsumer sub = session.createConsumer(topic0, "topic0");
            assertTrue("The subscription to deleted topic0 should not succeed", false);
            System.out.println("Succeed to subscribe: " + topic0);			  
          } catch (JMSException exc) {
            assertTrue("The subscription should not succeed", true);
            System.out.println("Fail to subscribe: " + topic0);
//            exc.printStackTrace();
          }

          // Try to subscribe to a remote inexistent topic.
          try {
            System.out.println("Try to subscribe: " + topic1);
            MessageConsumer sub = session.createConsumer(topic1, "topic1");
            assertTrue("The subscription to deleted topic1 should not succeed", false);
            System.out.println("Succeed to subscribe: " + topic1);        
          } catch (JMSException exc) {
            assertTrue("The subscription should not succeed", true);
            System.out.println("Fail to subscribe: " + topic1);
//            exc.printStackTrace();
          }

          // Try to subscribe to a topic located on an inexistent server.
          topic2 = new Topic("#10.10.1092");
          try {
            System.out.println("Try to subscribe: " + topic2);
            MessageConsumer sub = session.createConsumer(topic2, "topic2");
            assertTrue("The subscription to deleted topic2 should not succeed", false);
            System.out.println("Succeed to subscribe: " + topic2);        
          } catch (JMSException exc) {
            assertTrue("The subscription should not succeed", true);
            System.out.println("Fail to subscribe: " + topic2);
//            exc.printStackTrace();
          }
        }
      });
      subThread.start();

      Thread.sleep(1000);
      if (! subThread.isAlive()) return;
      
      Thread.sleep(10000);
      if (subThread.isAlive()) {
        subThread.interrupt();
        assertTrue("Subscription failed, test interupted", false);
      }
    } catch (Throwable exc) {
		  exc.printStackTrace();
		  error(exc);
	  } finally {
		  System.out.println("Server stop");
		  killAgentServer((short) 0);
      killAgentServer((short) 1);
		  endTest();
	  }
  }
}
