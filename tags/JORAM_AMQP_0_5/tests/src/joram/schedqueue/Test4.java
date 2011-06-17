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
package joram.schedqueue;

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

import framework.TestCase;

/**
 * Test the SchedulerQueue behavior after server stop and restart:
 * - Creates a SchedulerQueue then stop and restart the server.
 * - Test that this SchedulerQueue works correctly.
 * - Stops and restart the server and test again the behavior.
 * - Sends a scheduled message then stop and restart the server, verify that
 *   the message is delivered.
 */
public class Test4 extends framework.TestCase {
  static Connection cnx;
  static Session sess;

  public static void main(String args[]) throws Exception {
    new Test4().run();
  }
  
  public void run() {
    try {
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000L);

      int NbMsg = 1000;
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      
      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous");
      Queue queue = Queue.create(0, "schedulerQ", Queue.SCHEDULER_QUEUE, null);
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = sess.createConsumer(queue);
      MessageProducer prod = sess.createProducer(queue);
      cnx.start();

      long scheduleDate = System.currentTimeMillis() + 10000L;
      String msgid[] = new String[NbMsg];
      Message msg = null;
      
      int i;
      for (i=0; i<NbMsg; i++) {
        msg = sess.createMessage();
        msg.setLongProperty("scheduleDate", scheduleDate);
        prod.send(msg);
        msgid[i] = msg.getJMSMessageID();
        scheduleDate += 10L;
      }

      Thread.sleep(10000L);
      
      for (i=0; i<NbMsg; i++) {
        msg = cons.receive(60000);
        if (msg == null) break;
        assertTrue("Bad order of messages", msg.getJMSMessageID().equals(msgid[i]));
      }
      assertTrue("Bad number of msg #" + i, i == NbMsg);
      
      cnx.close();
      
      Thread.sleep(1000L);
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess.createConsumer(queue);
      prod = sess.createProducer(queue);
      cnx.start();

      scheduleDate = System.currentTimeMillis() + 10000L;
      
      for (i=0; i<NbMsg; i++) {
        msg = sess.createMessage();
        msg.setLongProperty("scheduleDate", scheduleDate);
        prod.send(msg);
        msgid[i] = msg.getJMSMessageID();
        scheduleDate += 10L;
      }

      cnx.close();
      
      TestCase.stopAgentServer((short)0);
      Thread.sleep(1000L);
      TestCase.startAgentServer((short)0);
      Thread.sleep(5000L);
      
      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess.createConsumer(queue);
      prod = sess.createProducer(queue);
      cnx.start();

      for (i=0; i<NbMsg; i++) {
        msg = cons.receive(30000);
        if (msg == null) break;
        assertTrue("Bad order of messages, expected " + msgid[i], msg.getJMSMessageID().equals(msgid[i]));
      }
      assertTrue("Bad number of msg #" + i, i == NbMsg);
      
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
