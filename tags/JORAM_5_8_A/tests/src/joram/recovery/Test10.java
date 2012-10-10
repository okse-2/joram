/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
package joram.recovery;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Test: Test the right behavior of persistence with denied messages (JORAM-59).
 */
public class Test10 extends framework.TestCase {
  static Connection cnx;
  static Session session;

  public static void main(String args[]) throws Exception {
    new Test10().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000L);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");
      User.create("anonymous", "anonymous");
      Queue queue = Queue.create();
      queue.setFreeReading();
      queue.setFreeWriting();
      AdminModule.disconnect();

      cnx = cf.createConnection();
      session = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = session.createProducer(queue);
      MessageConsumer cons = session.createConsumer(queue);
      cnx.start();

      Message msg = null;
      for (int i=0; i<15; i++) {
        msg = session.createMessage();
        msg.setIntProperty("idx", i);
        prod.send(msg);
        session.commit();
      }

      Thread.sleep(1000L);
      System.out.println("Stop Server#0");
      stopAgentServer((short) 0);
      Thread.sleep(1000L);
      System.out.println("Start Server#0");
      startAgentServer((short) 0);
      Thread.sleep(2000L);

      cnx = cf.createConnection();
      session = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      cons = session.createConsumer(queue);
      cnx.start();

      int idx;
      for (int i=0; i<5; i++) {
        msg = cons.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive and commit message: " + idx + ", " + msg);
        assertEquals(i, idx);
        session.commit();
      }

      msg = cons.receive();
      idx = msg.getIntProperty("idx");
      System.out.println("Receive and rollback message: " + idx + ", " + msg);
      session.rollback();
      Thread.sleep(1000L);
      
      for (int i=5; i<10; i++) {
        msg = cons.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive and commit message: " + idx + ", " + msg);
        assertEquals(i, idx);
        session.commit();
      }

      Thread.sleep(1000L);
      System.out.println("Stop Server#0");
      killAgentServer((short) 0);

      Thread.sleep(1000L);
      System.out.println("Start Server#0");
      startAgentServer((short) 0);
      Thread.sleep(2000L);

      cnx = cf.createConnection();
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = session.createConsumer(queue);
      cnx.start();

      for (int i=10; i<15; i++) {
        msg = cons.receive();
        idx = msg.getIntProperty("idx");
        System.out.println("Receive message: " + idx + ", " + msg);
        assertEquals(i, idx);
      }

      msg = cons.receive(1000L);
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
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
