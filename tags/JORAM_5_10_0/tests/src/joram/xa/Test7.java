/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent
 * Contributor(s): 
 */
package joram.xa;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
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
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test the right behavior of persistence with denied messages (JORAM-59).
 */
public class Test7 extends TestCase {

  public static void main(String[] args) {
    new Test7().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);
      Thread.sleep(1000L);
      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      XAConnectionFactory cf = (XAConnectionFactory) ictx.lookup("cf");
      ictx.close();

      // Create Connection and sessions
      XAConnection cnx = cf.createXAConnection();
      Session sessionp = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      XASession sessionc = cnx.createXASession();
      MessageProducer producer = sessionp.createProducer(queue);
      cnx.start();

      Message msg = null;
      for (int i=0; i<15; i++) {
        msg = sessionp.createMessage();
        msg.setIntProperty("idx", i);
        producer.send(msg);
        sessionp.commit();
      }
      sessionp.close();

      System.out.println("Stop Server#0");
      killAgentServer((short) 0);
      Thread.sleep(1000L);
      System.out.println("Start Server#0");
      startAgentServer((short) 0);
      Thread.sleep(2000L);

      cnx = cf.createXAConnection();
      sessionc = cnx.createXASession();
      MessageConsumer consumer = sessionc.createConsumer(queue);
      XAResource resource = sessionc.getXAResource();
      Xid xid = new XidImpl(new byte[0], 1, new String("" + System.currentTimeMillis()).getBytes());
      cnx.start();

      int idx;
      for (int i=0; i<5; i++) {
        resource.start(xid, XAResource.TMNOFLAGS);
        msg = consumer.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive and commit message: " + idx + ", " + msg);
        assertEquals(i, idx);
        resource.end(xid, XAResource.TMSUCCESS);
        resource.prepare(xid);
        resource.commit(xid, false);
      }

      resource.start(xid, XAResource.TMNOFLAGS);
      msg = consumer.receive();
      idx = msg.getIntProperty("idx");
      System.out.println("Receive and rollback message: " + idx + ", " + msg);
      resource.end(xid, XAResource.TMSUCCESS);
      resource.prepare(xid);
      resource.rollback(xid);
      Thread.sleep(1000L);
      
      for (int i=5; i<10; i++) {
        resource.start(xid, XAResource.TMNOFLAGS);
        msg = consumer.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive and commit message: " + idx + ", " + msg);
        assertEquals(i, idx);
        resource.end(xid, XAResource.TMSUCCESS);
        resource.prepare(xid);
        resource.commit(xid, false);
      }
      
      sessionc.close();
      cnx.close();

      System.out.println("Stop Server#0");
      killAgentServer((short) 0);
      Thread.sleep(1000L);
      System.out.println("Start Server#0");
      startAgentServer((short) 0);
      Thread.sleep(2000L);

      cnx = cf.createXAConnection();
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      consumer = session.createConsumer(queue);
      cnx.start();

      for (int i=10; i<15; i++) {
        msg = consumer.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive message: " + idx + ", " + msg);
        assertEquals(i, idx);
      }

      msg = consumer.receive(1000L);
      assertTrue(msg==null);
      if (msg != null) {
        idx = msg.getIntProperty("idx");
        System.out.println("Receive message: " + idx + ", " + msg);
      }
      
      session.close();
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
   * Administration: Creates and registers administered objects.
   */
  public void admin() throws Exception {
    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    
    AdminModule.connect(cf, "root", "root");
    
    User.create("anonymous", "anonymous");
    Queue queue = org.objectweb.joram.client.jms.Queue.create("queue");
    queue.setFreeReading();
    queue.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
