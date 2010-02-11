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
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;


/**
 * Test the fonctioning of asynchronous and synchronous subscrption. 
 * Using a Topic.
 */
public class Sub extends TestCase {
  
  public static void main(String[] args) {
    new Sub().run();
  }

  public void run() {
    try {
      
      System.out.println("server start");
      startAgentServer((short) 0);
      // for topic agent.
      startAgentServer((short) 1);

      admin();
      System.out.println("admin config ok");
      
      killAgentServer((short) 1);

      Context ictx = new InitialContext();
      final Topic topic = (Topic) ictx.lookup("topic");
      System.out.println("Topic = " + topic);
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      // connection for subscriber
      Connection cnx = cf.createConnection();
      final Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      for (int i = 0; i < 2; i++) {
        // Create consumer 
        boolean asyncSub = (i==0);
        System.out.println("\nTest with asyncSub = " + asyncSub);
        ((org.objectweb.joram.client.jms.Session) session).setAsyncSub(asyncSub);
        Thread subTheard = new Thread(new Runnable() {
          public void run() {
            // subscribe
            MessageConsumer sub;
            try {
              sub = session.createConsumer(topic, "topic");
              System.out.println("sub = "  + sub);
            } catch (JMSException e) {

            }
          }
        });
        subTheard.start();

        Thread.sleep(1000);
        if (subTheard.isAlive()) {
          System.out.println("createConsumer lock... (wait 10s).");
          Thread.sleep(10000);
          if (subTheard.isAlive() && asyncSub) {
            fail("test failed asyncSub = " + asyncSub);
          }
          subTheard.interrupt();
          System.out.println("createConsumer interrupt.");
        }
      }

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  /**
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // conexion
    AdminModule.connect("localhost", 2560,
        "root", "root", 60);
    // create a Topic
    Topic topic = org.objectweb.joram.client.jms.Topic.create(1, "topic");

    // create a user
    User.create("anonymous", "anonymous");
    // set permissions
    ((org.objectweb.joram.client.jms.Topic) topic).setFreeReading();
    ((org.objectweb.joram.client.jms.Topic) topic).setFreeWriting();

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.rebind("cf", cf);
    jndiCtx.rebind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
