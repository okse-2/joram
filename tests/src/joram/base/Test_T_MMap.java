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
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import framework.TestCase;

/**
 * Test : The Map message received by the consumer is the same that the Map
 * message sent by the producer Use a Topic
 */
public class Test_T_MMap extends TestCase implements javax.jms.MessageListener {

  private MapMessage pMsg; // store text message send by pruducer
  public byte[] content;

  public static void main(String[] args) {
    new Test_T_MMap().run();
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

      // create a Map message sent to the topic by the producer 
      MapMessage msg = sessionp.createMapMessage();
      content = new byte[10];
      for (int i = 0; i < 10; i++)
        content[i] = (byte) i;

      msg.setBytes("tab_byte", content);
      msg.setByte("byte", (byte) 15);
      msg.setBoolean("bool", true);

      msg.setChar("char", 'e');
      msg.setString("string", "it is a string");

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

    ConnectionFactory cf = org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  public void onMessage(Message message) {
    System.out.println("message received");
    try {
      MapMessage msg = (MapMessage) message;
      MapMessage msgP = getProducerMessage();
      byte[] receive = new byte[10];
      // no order for read with map
      String rst = msg.getString("string");
      char rch = msg.getChar("char");
      byte rbyte = msg.getByte("byte");
      boolean rbool = msg.getBoolean("bool");
      receive = msg.getBytes("tab_byte");

      //test message
      assertEquals(msgP.getJMSMessageID(), msg.getJMSMessageID());
      assertEquals(msgP.getJMSType(), msg.getJMSType());
      assertEquals(msgP.getJMSDestination(), msg.getJMSDestination());
      assertEquals(content, receive, 10);
      assertEquals("it is a string", rst);
      assertEquals('e', rch);
      assertEquals(true, rbool);
      assertEquals((byte) 15, rbyte);

    } catch (Throwable thr) {
      addError(thr);
    }
  }

  public void setProducerMessage(MapMessage message) {
    pMsg = message;
  }

  public MapMessage getProducerMessage() {
    return pMsg;
  }
}
