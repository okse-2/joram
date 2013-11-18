/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.proxies.ConnectionManager;

import framework.TestCase;

/**
 * Test :
 * 
 */
public class TestProxyActivation extends TestCase {

  public static void main(String[] args) {
    new TestProxyActivation().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);
      Thread.sleep(2000L);
      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // create a producer
      MessageProducer producer = sessionp.createProducer(queue);
      // create a message send to the queue by the pruducer
      Message msg = sessionp.createMessage();
      msg.setJMSCorrelationID("123456");
      producer.send(msg);
      System.out.println("send message.");
      
      // Deactivate the Proxy
      AdminModule.connect(cf, "root", "root");
      AdminModule.invokeStaticServerMethod(ConnectionManager.class.getName(), "setActivate", new Class[] {boolean.class}, new Boolean[] {false});
      System.out.println("ConnectionManager deactivated.");
      
      Thread.sleep(1000);
      
      try {
        Connection cnx1 = cf.createConnection();
        Session sessionc = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
        fail("wait an IllegalStateException...");
      } catch (Exception exc) {
        System.out.println("waiting exception: " + exc);
        assertTrue("The exception is not an IllegalStateException", exc instanceof javax.jms.IllegalStateException);
      }
      
      // reactivate the Proxy
      AdminModule.invokeStaticServerMethod(ConnectionManager.class.getName(), "setActivate", new Class[] {boolean.class}, new Boolean[] {true});
      System.out.println("ConnectionManager activated.");
      AdminModule.disconnect();
      
      Thread.sleep(1000);
      
      Connection cnx1 = cf.createConnection();
      Session sessionc = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = sessionc.createConsumer(queue);
      cnx1.start();
      System.out.println("receive...");
      // the consumer receive the message from the queue
      Message msg1 = consumer.receive();
      System.out.println("msg = " + msg1.getJMSCorrelationID() + ", msg = " + msg.getJMSCorrelationID());
      // test messages, are same !
      assertEquals(msg.getJMSCorrelationID(), msg1.getJMSCorrelationID());

      cnx.close();
      cnx1.close();
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
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // conexion
    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    AdminModule.connect(cf, "root", "root");
    
    // create a Queue
    Queue queue = Queue.create("queue");

    // create a user
    User user = User.create("anonymous", "anonymous");
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
