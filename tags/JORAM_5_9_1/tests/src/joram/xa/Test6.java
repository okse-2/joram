/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 - ScalAgent Distributed Technologies
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
package joram.xa;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.XidImpl;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;

import framework.TestCase;

/**
 * Test : rollback 
 * - on receive
 * 
 */
public class Test6 extends TestCase {

  public static void main(String[] args) {
    new Test6().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      XAConnectionFactory cf = (XAConnectionFactory) ictx.lookup("cf");
      ictx.close();

      XAConnection cnx = cf.createXAConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      XASession sessionc = cnx.createXASession();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(queue);
      
      MessageConsumer consumer = sessionc.createConsumer(queue);
      XAResource resource = sessionc.getXAResource();
      Xid xid1 = new XidImpl(new byte[0], 1, new String("" + System.currentTimeMillis()).getBytes());
      resource.start(xid1, XAResource.TMNOFLAGS);
      cnx.start();

      // create a text message send to the queue
      TextMessage msg = sessionp.createTextMessage();
      msg.setText("my message");
      producer.send(msg);

      // test rollback on receive
      TextMessage msg1 = (TextMessage) consumer.receive(1000L);
      System.out.println("msg getJMSRedelivered = " + msg1.getJMSRedelivered());
      assertEquals("Bad value for JMSRedelivered.", false, msg1.getJMSRedelivered());
      
      resource.end(xid1, XAResource.TMSUCCESS);
      resource.prepare(xid1);
      resource.rollback(xid1);

      xid1 = new XidImpl(new byte[0], 1, new String("" + System.currentTimeMillis()).getBytes());
      resource.start(xid1, XAResource.TMNOFLAGS);
      
      // receive the message
      msg1 = (TextMessage) consumer.receive(1000L);
      System.out.println("re msg getJMSRedelivered = " + msg1.getJMSRedelivered());
      assertEquals("Bad value for JMSRedelivered.", true, msg1.getJMSRedelivered());
      
      resource.end(xid1, XAResource.TMSUCCESS);
      resource.prepare(xid1);
      resource.commit(xid1, false);

      cnx.close();
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
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue
    Queue queue = Queue.create("queue");

    // create a user
    User user = User.create("anonymous", "anonymous");
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    XAConnectionFactory cf = XATcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
