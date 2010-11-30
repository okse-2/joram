
/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.base;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.MessageErrorConstants;

import framework.TestCase;

/**
 * Test that a message body is not deleted on a DMQ after server restart
 */
public class Test_DMQ_Body extends TestCase {

  public static void main(String[] args) {
    new Test_DMQ_Body().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);

      Thread.sleep(1000);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      // create a connection 
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(queue);
      MessageConsumer consumer = sessionc.createConsumer(queue);
 
      // create a message send to the queue by the pruducer 
      TextMessage msg = sessionp.createTextMessage("test not delete");
      producer.setTimeToLive(1000L);
      producer.send(msg);

      // sleep to be safe that the message expired
      Thread.sleep(1500L);
      // The receive reaction will send the message in the DMQ it is not
      // already done.
      assertEquals(null, consumer.receive(100));
      
      cnx.close();
      
      test(msg);
      
      stopAgentServer((short) 0);
      Thread.sleep(2000);
      startAgentServer((short) 0);
      Thread.sleep(2000);
      
      test(msg);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }

  public void test(TextMessage msg) throws Exception {
    InitialContext ictx = new InitialContext();
    DeadMQueue dq = (DeadMQueue) ictx.lookup("dq");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx_dead = cf.createConnection();
    Session sessioncdead = cnx_dead.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cnx_dead.start();

    QueueBrowser browserDmq = sessioncdead.createBrowser(dq);
    Enumeration messages = browserDmq.getEnumeration();
    
    assertTrue(messages.hasMoreElements());

    TextMessage msg1 = (TextMessage) messages.nextElement();

    assertEquals(1, msg1.getIntProperty("JMS_JORAM_ERRORCOUNT"));
    assertEquals(MessageErrorConstants.EXPIRED, msg1.getIntProperty("JMS_JORAM_ERRORCODE_1"));
    
    System.out.println("msg#" + msg1.getJMSMessageID() + " " + msg1.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));

    assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
    assertEquals(msg.getJMSType(), msg1.getJMSType());
    assertEquals(msg.getText(), msg1.getText());

    assertFalse(messages.hasMoreElements());
    
    cnx_dead.close();
  }
  
  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // conexion 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue   
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(0);

    // create a deadqueue for receive expired messages
    DeadMQueue dq = (DeadMQueue) DeadMQueue.create(0);
    dq.setFreeReading();
    dq.setFreeWriting();
    AdminModule.setDefaultDMQ(0, dq);

    // create a user
    User.create("anonymous", "anonymous");
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("dq", dq);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
