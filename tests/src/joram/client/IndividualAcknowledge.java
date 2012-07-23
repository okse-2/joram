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
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : Test the Joram INDIVIDUAL_ACKNOWLEDGE.
 * 
 * - Send 4 messages on a queue,
 * - Receive the 4 messages and acknowledge the message 2 and 3
 * - close the consumer session.
 * - reopen and receive 3 messages, expected message 1 and 4
 *   and the third message is null
 * 
 * @see JORAM-52
 */
public class IndividualAcknowledge extends TestCase {

  public static void main(String[] args) {
    new IndividualAcknowledge().run();
  }
  
  private static Queue queue;
  private static ConnectionFactory cf;

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      admin();
      System.out.println("admin config ok");

      Connection cnx = cf.createConnection();
      Session sessionp = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = (Session) cnx.createSession(false, Session.INDIVIDUAL_ACKNOWLEDGE);

      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(null);
      MessageConsumer consumer = sessionc.createConsumer(queue);
      
      System.out.println("getAcknowledgeMode = " + sessionc.getAcknowledgeMode());

      for (int i = 0; i < 4; i++) {
        // create a text message send to the queue by the producer 
        TextMessage msg = sessionp.createTextMessage("message_" + i);
        producer.send(queue, msg);
        System.out.println("send message_" + i);
      }

      Message msg1 = consumer.receiveNoWait();
      Message msg2 = consumer.receiveNoWait();
      Message msg3 = consumer.receiveNoWait();
      Message msg4 = consumer.receiveNoWait();
      
      msg2.acknowledge();
      msg3.acknowledge();
      System.out.println("acknowledge msg 2 and 3.");
      
      Thread.sleep(2000);
      consumer.close();
      sessionc.close();
      System.out.println("close session (deny).");
      
      sessionc = (Session) cnx.createSession(false, Session.INDIVIDUAL_ACKNOWLEDGE);
      consumer = sessionc.createConsumer(queue);
      
      Message msg = consumer.receiveNoWait();
      if (msg != null)
      System.out.println("msg = " + ((TextMessage)msg).getText());
      assertEquals("The received message is not the expected.", "message_0", ((TextMessage)msg).getText());
      
      msg = consumer.receiveNoWait();
      if (msg != null)
      System.out.println("msg = " + ((TextMessage)msg).getText());
      assertEquals("The received message is not the expected.", "message_3", ((TextMessage)msg).getText());
      
      msg = consumer.receiveNoWait();
      assertEquals("The msg must be null", null, msg);
      
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
    
    // create destinations   
    queue = Queue.create("queue"); 
    queue.setFreeReading();
    queue.setFreeWriting();

    cf = TcpConnectionFactory.create("localhost", 2560);

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}

