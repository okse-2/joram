/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.pooled;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.pool.PooledConnectionFactory;

import framework.TestCase;

/**
 * Test the PooledConnectionFactory with various users, verify that different
 * passwords returns errors.
 */
public class Test4 extends TestCase {
  static ConnectionFactory cf, pcf;
  static Queue queue;
  static Object lock = new Object();
  static int nbt;

  /**
   * @param args
   */
  public static void main(String[] args) {
    new Test4().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      // Creates the ConnectionFactory 
      cf = TcpConnectionFactory.create("localhost", 16010);
      pcf = new PooledConnectionFactory(cf);

      // Creates the administered objects
      AdminModule.connect(cf);

      // create a Queue and a different users  
      queue = Queue.create(0);
      User.create("anonymous", "anonymous");
      User.create("anonymous2", "anonymous2");

      // set permissions
      queue.setFreeReading();
      queue.setFreeWriting();

      Context jndiCtx = new InitialContext();
      jndiCtx.bind("cf", cf);
      jndiCtx.bind("queue", queue);
      jndiCtx.close();

      AdminModule.disconnect();

      Connection cnx1 = pcf.createConnection("anonymous", "anonymous");
      System.out.println("cnx1=" + cnx1);
      Session session = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      producer.setTimeToLive(1000);
      
      for (int j=0; j<10; j++) {
        Message msg = session.createMessage();
        producer.send(msg);
        try {
          Thread.sleep(10);
        } catch (InterruptedException exc) {}
      }
      cnx1.close();
      
      Connection cnx2 = pcf.createConnection("anonymous2", "anonymous2");
      System.out.println("cnx2=" + cnx2);
      assertFalse("Connexion for anonymous2 is same than anonymous", (cnx1 == cnx2));      
      session = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = session.createProducer(queue);
      producer.setTimeToLive(1000);
      
      for (int j=0; j<10; j++) {
        Message msg = session.createMessage();
        producer.send(msg);
        try {
          Thread.sleep(10);
        } catch (InterruptedException exc) {}
      }
      cnx2.close();
      
      Connection cnx3 = pcf.createConnection("anonymous", "anonymous");
      System.out.println("cnx3=" + cnx3);
      assertTrue("Connexion for anonymous is new", (cnx1 == cnx3));
      cnx3.close();
      
      Connection cnx4 = pcf.createConnection("anonymous2", "anonymous2");
      System.out.println("cnx4=" + cnx4);
      assertTrue("Connexion for anonymous2 is new", (cnx2 == cnx4));
      cnx4.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
