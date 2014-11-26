/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.dmq;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.MessageErrorConstants;

import framework.TestCase;

/**
 * Some messages with a TTL are sent to a queue. When they are expired, they go
 * to the dmqueue0. When the dmqueue0 is full, the additional messages go to
 * dmqueue1. When the dmqueue1 is full, the additional messages go to dmqueue2,
 * and so on.
 */
public class ChainedDMQ extends TestCase {

  private static int NB_MESSAGES = 75;
  private static int DMQ0_MAX_SIZE = 10;
  private static int DMQ1_MAX_SIZE = 10;
  private static int DMQ2_MAX_SIZE = 10;
  private static int DMQ3_MAX_SIZE = 10;
  private static int DMQ4_MAX_SIZE = 10;

  public static void main(String[] args) {
    new ChainedDMQ().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      Queue dmqueue0 = (Queue) ictx.lookup("dmqueue0");
      Queue dmqueue1 = (Queue) ictx.lookup("dmqueue1");
      Queue dmqueue2 = (Queue) ictx.lookup("dmqueue2");
      Queue dmqueue3 = (Queue) ictx.lookup("dmqueue3");
      Queue dmqueue4 = (Queue) ictx.lookup("dmqueue4");
      Queue dmqueue5 = (Queue) ictx.lookup("dmqueue5");
      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ictx.close();

      Connection cnx = cf0.createConnection();
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      MessageConsumer consumer = session.createConsumer(queue);
      MessageProducer producer = session.createProducer(queue);

      TextMessage msg = null;
      TextMessage msg1 = null;

      for (int j = 0; j < NB_MESSAGES; j++) {
        msg = session.createTextMessage();
        msg.setText("messagedist#" + j);
        producer.send(msg, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 1000);
      }

      // Waiting for the messages to be out of date
      Thread.sleep(2000);

      msg1 = (TextMessage) consumer.receive(500);
      assertEquals(null, msg1);

      // Messages should be present on the DMQ
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      assertEquals(DMQ0_MAX_SIZE, dmqueue0.getPendingMessages());
      assertEquals(DMQ1_MAX_SIZE, dmqueue1.getPendingMessages());
      assertEquals(DMQ2_MAX_SIZE, dmqueue2.getPendingMessages());
      assertEquals(DMQ3_MAX_SIZE, dmqueue3.getPendingMessages());
      assertEquals(DMQ4_MAX_SIZE, dmqueue4.getPendingMessages());
      assertEquals(NB_MESSAGES - DMQ0_MAX_SIZE - DMQ1_MAX_SIZE - DMQ2_MAX_SIZE - DMQ3_MAX_SIZE
          - DMQ4_MAX_SIZE, dmqueue5.getPendingMessages());
      AdminModule.disconnect();

      // Check some message properties on DMQ 0
      consumer = session.createConsumer(dmqueue0);
      msg = (TextMessage) consumer.receive();
      assertEquals(1, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      consumer.close();
      
      // Check some message properties on DMQ 1
      consumer = session.createConsumer(dmqueue1);
      msg = (TextMessage) consumer.receive();
      assertEquals(2, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_2"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_2"));
      consumer.close();

      // Check some message properties on DMQ 2
      consumer = session.createConsumer(dmqueue2);
      msg = (TextMessage) consumer.receive();
      assertEquals(3, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_2"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_2"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_3"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_3"));
      consumer.close();

      // Check some message properties on DMQ 3
      consumer = session.createConsumer(dmqueue3);
      msg = (TextMessage) consumer.receive();
      assertEquals(4, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_2"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_2"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_3"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_3"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_4"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_4"));
      consumer.close();

      // Check some message properties on DMQ 4
      consumer = session.createConsumer(dmqueue4);
      msg = (TextMessage) consumer.receive();
      assertEquals(5, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_2"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_2"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_3"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_3"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_4"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_4"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_5"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_5"));
      consumer.close();

      // Check some message properties on DMQ 5
      consumer = session.createConsumer(dmqueue5);
      msg = (TextMessage) consumer.receive();
      assertEquals(6, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_2"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_2"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_3"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_3"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_4"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_4"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_5"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_5"));
      assertEquals(MessageErrorConstants.QUEUE_FULL, msg.getIntProperty("JMS_JORAM_ERRORCODE_6"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_6"));
      consumer.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Servers stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    Properties prop = new Properties();
    prop.setProperty("period", "1000");

    // connection
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    
    // create a Queue
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(0, prop);
    queue.setFreeReading();
    queue.setFreeWriting();
    
    // create DMQs
    Queue dmqueue0 = (Queue) Queue.create(0);
    dmqueue0.setFreeReading();
    dmqueue0.setFreeWriting();
    dmqueue0.setNbMaxMsg(DMQ0_MAX_SIZE);
    Queue dmqueue1 = (Queue) Queue.create(0);
    dmqueue1.setFreeReading();
    dmqueue1.setFreeWriting();
    dmqueue1.setNbMaxMsg(DMQ1_MAX_SIZE);
    Queue dmqueue2 = (Queue) Queue.create(0);
    dmqueue2.setFreeReading();
    dmqueue2.setFreeWriting();
    dmqueue2.setNbMaxMsg(DMQ2_MAX_SIZE);
    Queue dmqueue3 = (Queue) Queue.create(0);
    dmqueue3.setFreeReading();
    dmqueue3.setFreeWriting();
    dmqueue3.setNbMaxMsg(DMQ3_MAX_SIZE);
    Queue dmqueue4 = (Queue) Queue.create(0);
    dmqueue4.setFreeReading();
    dmqueue4.setFreeWriting();
    dmqueue4.setNbMaxMsg(DMQ4_MAX_SIZE);
    Queue dmqueue5 = (Queue) Queue.create(0);
    dmqueue5.setFreeReading();
    dmqueue5.setFreeWriting();

    dmqueue0.setDMQ(dmqueue1);
    dmqueue1.setDMQ(dmqueue2);
    dmqueue2.setDMQ(dmqueue3);
    dmqueue3.setDMQ(dmqueue4);
    dmqueue4.setDMQ(dmqueue5);

    queue.setDMQ(dmqueue0);

    // create a user
    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

    ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("dmqueue0", dmqueue0);
    jndiCtx.bind("dmqueue1", dmqueue1);
    jndiCtx.bind("dmqueue2", dmqueue2);
    jndiCtx.bind("dmqueue3", dmqueue3);
    jndiCtx.bind("dmqueue4", dmqueue4);
    jndiCtx.bind("dmqueue5", dmqueue5);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
