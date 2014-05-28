/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 ScalAgent Distributed Technologies
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
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : Throw a JMSSecurityException if the queue is full.
 * 
 * - create queue and DMQ.
 * - set queue nbMaxMsg to 2
 * - set SyncExceptionOnFull
 * - try to send 4 messages, must catch a JMSSecurityException on the third send.
 * - verify that the DMQ is empty.
 * 
 * @see JORAM-32
 */
public class SyncExceptionOnFullQueue extends TestCase {

  public static void main(String[] args) {
    new SyncExceptionOnFullQueue().run();
  }
  
  private static Queue queue;
  private static Queue dmq;
  private static ConnectionFactory cf;

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      admin();
      System.out.println("admin config ok");

      Connection cnx = cf.createConnection();
      Connection cnxdmq = cf.createConnection("dmq","dmq");
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessioncdmq = cnxdmq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);

      cnx.start();
      cnxdmq.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(null);
      MessageConsumer consumerdmq = sessioncdmq.createConsumer(dmq);
      MessageConsumer consumer = sessionc.createConsumer(queue);

      int i = 0;
      try {
        for (i = 0; i < 4; i++) {
          // create a text message send to the queue by the producer 
          TextMessage msg = sessionp.createTextMessage("message_" + i);
          producer.send(queue, msg);
          System.out.println("send message_" + i);
        }
      } catch (javax.jms.JMSSecurityException e) {
        System.out.println("Waiting exception");
      }
      assertEquals("Nb msg sent", 2, i);

      Message msg = consumerdmq.receive(5000);
      System.out.println("DMQ msg = " + msg);
      assertEquals("The DMQ contains message", null, msg);

      int rec = 0;
      msg = consumer.receive(5000);
      while (msg != null) {
        rec++;
        msg = consumer.receive(5000);
      }
     System.out.println("Nb received = " + rec);
     assertEquals("Nb msg received", 2, rec);
     
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short)0);
      endTest(); 
    }
  }

  /**
   * Admin : Create queue and a user anonymous
   * set NbMaxMsg for this queue
   */
  public void admin() throws Exception {
    // connection 
    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560, "root", "root", 60);

    // create users
    User user = User.create("anonymous", "anonymous");
    User userdmq = User.create("dmq", "dmq");
    
    // create destinations   
    queue = Queue.create("queue"); 
    queue.setFreeReading();
    queue.setFreeWriting();

    // create DMQs
    dmq = Queue.create(0);
    dmq.setReader(userdmq);
    dmq.setFreeWriting();

    queue.setDMQ(dmq);
    queue.setNbMaxMsg(2);
    queue.setSyncExceptionOnFull(true);

    cf = TcpConnectionFactory.create("localhost", 2560);

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}

