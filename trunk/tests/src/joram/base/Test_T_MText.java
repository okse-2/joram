/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2010 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : The text message received by the consumer is the same that the text
 * message sent by the producer Use a Topic
 */
public class Test_T_MText extends TestCase implements javax.jms.MessageListener {

  private TextMessage pMsg; // store text message send by pruducer

  public static void main(String[] args) {
    new Test_T_MText().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Topic topic = (Topic) ictx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(topic);
      MessageConsumer consumer = sessionc.createConsumer(topic);

      // the consumer records on the topic
      consumer.setMessageListener(this);
      TextMessage msg = sessionp.createTextMessage();
      msg.setText("message_type_text");

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
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Topic   
    org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic.create("topic");

    // create a user
    User.create("anonymous", "anonymous");

    // set permissions
    topic.setFreeReading();
    topic.setFreeWriting();

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  public void onMessage(Message message) {
    System.out.println("message received");
    try {
      TextMessage msg = (TextMessage) message;
      TextMessage msgP = getProducerMessage();
      //test message
      assertEquals(msgP.getJMSMessageID(), msg.getJMSMessageID());
      assertEquals(msgP.getJMSType(), msg.getJMSType());
      assertEquals(msgP.getJMSDestination(), msg.getJMSDestination());
      assertEquals("message_type_text", msg.getText());
    } catch (Throwable exc) {
      addError(exc);
    }
  }

  public void setProducerMessage(TextMessage message) {
    pMsg = message;
  }

  public TextMessage getProducerMessage() {
    return pMsg;
  }
}
