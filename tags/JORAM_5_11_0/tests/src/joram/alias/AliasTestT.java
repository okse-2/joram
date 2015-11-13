/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2011 - 2013 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: The message received by the consumer is the same that the message sent
 * by the producer using distribution and acquisition topic to build an alias topic.
 */
public class AliasTestT extends TestCase implements javax.jms.MessageListener {

  private Message pMsg; // store message send by producer

  public static void main(String[] args) {
    new AliasTestT().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Topic tdist = (Topic) ictx.lookup("tdist");
      Topic tack = (Topic) ictx.lookup("tack");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(tdist);
      MessageConsumer consumer = sessionc.createConsumer(tack);

      // the consumer records on the topic
      // the consumer records on the topic
      consumer.setMessageListener(this);
      TextMessage msg = sessionp.createTextMessage();
      msg.setText("Some message content, foo\n bar!");
      msg.setStringProperty("foo", "bar");
      setProducerMessage(msg);
      producer.send(msg);

      // Wait to receive the message.
      Thread.sleep(1000);

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

    /* creating acquisition and distribution topics */

    Properties propAckTopic = new Properties();
    propAckTopic.put("acquisition.className", "org.objectweb.joram.mom.dest.VoidAcquisitionHandler");
    Topic tack = Topic.create(0, "tack", Topic.ACQUISITION_TOPIC, propAckTopic);

    Properties propDistTopic = new Properties();
    propDistTopic.put("distribution.className",
        "org.objectweb.joram.mom.dest.NotificationDistributionHandler");
    propDistTopic.put("remoteAgentID", tack.getName());
    Topic tdist = Topic.create(0, "tdist", Topic.DISTRIBUTION_TOPIC, propDistTopic);

    User.create("anonymous", "anonymous");

    tack.setFreeReading();
    tdist.setFreeReading();
    tack.setFreeWriting();
    tdist.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();

    jndiCtx.bind("cf", cf);
    jndiCtx.bind("tack", tack);
    jndiCtx.bind("tdist", tdist);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  /* (non-Javadoc)
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message) */
  public void onMessage(Message message) {
    System.out.println("message received");
    try {
      Message msgP = getProducerMessage();
      //test messages
      assertEquals(msgP.getJMSType(), message.getJMSType());
      assertEquals(((TextMessage) msgP).getText(), ((TextMessage) message).getText());
      assertEquals(msgP.getStringProperty("foo"), message.getStringProperty("foo"));
    } catch (javax.jms.JMSException JE) {
      JE.printStackTrace();
    }
  }

  public void setProducerMessage(Message message) {
    pMsg = message;
  }

  public Message getProducerMessage() {
    return pMsg;
  }
}
