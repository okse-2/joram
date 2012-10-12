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
 * Initial developer(s):
 * Contributor(s): 
 */
package joram.alias;

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

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : The message received by the consumer is the same that the message sent
 * by the producer Use two alias queues
 */
public class AliasTestQ extends TestCase {

  private static String MESSAGE_CONTENT = "Scalagent Distributed Technologies";

  public static void main(String[] args) {
    new AliasTestQ().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue qdist = (Queue) ictx.lookup("qdist");
      Queue qack = (Queue) ictx.lookup("qack");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(qdist);
      MessageConsumer consumer = sessionc.createConsumer(qack);
      // create a message send to the queue by the producer
      TextMessage msg = sessionp.createTextMessage();
      msg.setText(MESSAGE_CONTENT);
      msg.setStringProperty("foo", "bar");

      producer.send(msg);

      // the consumer receive the message from the queue
      Message msg1 = consumer.receive();

      // test messages, are same !
      assertEquals(msg.getJMSType(), msg1.getJMSType());
      assertEquals(msg.getText(), ((TextMessage) msg1).getText());
      assertEquals(msg.getStringProperty("foo"), msg1.getStringProperty("foo"));

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
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {

    AdminModule.connect("localhost", 2560, "root", "root", 60);

    /* creating acquisition and distribution queues */

    Properties propAckQueue = new Properties();
    propAckQueue.put("acquisition.className", "org.objectweb.joram.mom.dest.VoidAcquisitionHandler");
    Queue qack = Queue.create(0, "qack", Queue.ACQUISITION_QUEUE, propAckQueue);

    Properties propDistQueue = new Properties();
    propDistQueue.put("distribution.className",
        "org.objectweb.joram.mom.dest.NotificationDistributionHandler");
    propDistQueue.put("remoteAgentID", qack.getName());
    Queue qdist = Queue.create(0, "qdist", Queue.DISTRIBUTION_QUEUE, propDistQueue);

    User.create("anonymous", "anonymous");

    qack.setFreeReading();
    qdist.setFreeReading();

    qack.setFreeWriting();
    qdist.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();

    jndiCtx.bind("cf", cf);
    jndiCtx.bind("qack", qack);
    jndiCtx.bind("qdist", qdist);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
