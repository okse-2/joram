/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
package joram.schedqueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.SchedulerQueue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test the SchedulerQueue behavior after server stop and restart:
 * - Creates a SchedulerQueue then stop and restart the server.
 * - Test that this SchedulerQueue works correctly.
 * - Stops and restart the server and test again the behavior.
 * - Sends a scheduled message then stop and restart the server, verify that
 *   the message is delivered.
 */
public class Test3 extends framework.TestCase {
  static Connection cnx;
  static Session sess;

  public static void main(String args[]) throws Exception {
    new Test3().run();
  }
  
  public void run() {
    try {
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000L);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      
      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous");
      Queue queue = SchedulerQueue.create(0, "schedulerQ");
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();

      TestCase.stopAgentServer((short)0);
      Thread.sleep(1000L);
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000L);
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = sess.createConsumer(queue);
      MessageProducer prod = sess.createProducer(queue);
      cnx.start();

      long scheduleDate = System.currentTimeMillis() + 5000;
      Message msg = sess.createMessage();
      msg.setLongProperty("scheduleDate", scheduleDate);
      prod.send(msg);
      String msgid = msg.getJMSMessageID();
      System.out.println("Send #" + msgid + ", " + scheduleDate);
      
      msg = cons.receive(10000);
      assertTrue(msg != null);
      if (msg != null) {
        long receiveDate = System.currentTimeMillis();
        Object scheduled = msg.getObjectProperty("scheduleDate");
        System.out.println("Receive #" + msg.getJMSMessageID() + ": " + (receiveDate - scheduleDate) + ", " + scheduled);
        assertTrue((receiveDate-scheduleDate) < 1000);
      } else {
        System.out.println("Receive null");
      }
      
      cnx.close();

      TestCase.stopAgentServer((short)0);
      Thread.sleep(1000L);
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000L);
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess.createConsumer(queue);
      prod = sess.createProducer(queue);
      cnx.start();

      scheduleDate = System.currentTimeMillis() + 5000;
      msg = sess.createMessage();
      msg.setLongProperty("scheduleDate", scheduleDate);
      prod.send(msg);
      msgid = msg.getJMSMessageID();
      System.out.println("Send #" + msgid + ", " + scheduleDate);
      
      msg = cons.receive(10000);
      assertTrue(msg != null);
      if (msg != null) {
        long receiveDate = System.currentTimeMillis();
        Object scheduled = msg.getObjectProperty("scheduleDate");
        System.out.println("Receive #" + msg.getJMSMessageID() + ": " + (receiveDate - scheduleDate) + ", " + scheduled);
        assertTrue((receiveDate-scheduleDate) < 1000);
      } else {
        System.out.println("Receive null");
      }

      scheduleDate = System.currentTimeMillis() + 10000;
      msg = sess.createMessage();
      msg.setLongProperty("scheduleDate", scheduleDate);
      prod.send(msg);
      msgid = msg.getJMSMessageID();
      System.out.println("Send #" + msgid + ", " + scheduleDate);
      
      cnx.close();

      TestCase.stopAgentServer((short)0);
      Thread.sleep(1000L);
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000L);
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess.createConsumer(queue);
      prod = sess.createProducer(queue);
      cnx.start();
      
      msg = cons.receive(15000);
      assertTrue(msg != null);
      if (msg != null) {
        long receiveDate = System.currentTimeMillis();
        Object scheduled = msg.getObjectProperty("scheduleDate");
        System.out.println("Receive #" + msg.getJMSMessageID() + ": " + (receiveDate - scheduleDate) + ", " + scheduled);
        assertTrue((receiveDate-scheduleDate) < 1000);
      } else {
        System.out.println("Receive null");
      }
      
      cnx.close();
    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      TestCase.stopAgentServer((short)0);
      endTest();
    }
  }
}
