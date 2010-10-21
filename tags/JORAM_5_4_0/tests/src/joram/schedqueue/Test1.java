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

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * 
 *
 */
public class Test1 extends framework.TestCase {
  static Connection cnx;
  static Session sess;

  public static void main(String args[]) throws Exception {
    new Test1().run();
  }
  
  public void run() {
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      User user = User.create("anonymous", "anonymous");
      Queue queue = Queue.create(0, "schedulerQ", Queue.SCHEDULER_QUEUE, null);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      AdminModule.disconnect();

      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_a = sess.createConsumer(queue);
      MessageProducer prod_a = sess.createProducer(queue);

      cnx.start();

      long scheduleDate = System.currentTimeMillis() + 5000;
      Message msg = sess.createMessage();
      // msg.setIntProperty("Index", 0);
      msg.setLongProperty("scheduleDate", scheduleDate);
      System.out.println("send");
      prod_a.send(msg);

      msg = cons_a.receive();
      long receiveDate = System.currentTimeMillis();
      // System.out.println("avant "+scheduleDate);
      // System.out.println("apres "+receiveDate);
      // int index = msg.getIntProperty("Index");
      assertTrue((receiveDate-scheduleDate) < 1000);

      Thread.sleep(1000L);
      scheduleDate = System.currentTimeMillis();
      msg = sess.createMessage();
      // msg.setIntProperty("Index", 0);
      msg.setLongProperty("scheduleDate", scheduleDate-5000);
      System.out.println("send");
      prod_a.send(msg);
      msg = cons_a.receive();
      receiveDate = System.currentTimeMillis();
      // receive immediately
      assertTrue((receiveDate-scheduleDate) < 1000);

      cnx.close();
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
