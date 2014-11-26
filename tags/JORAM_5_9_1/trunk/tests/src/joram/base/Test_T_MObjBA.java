/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2008 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;


/**
 * Test :
 *     The object message received by the consumer is the same that 
 *     the object message sent by the producer 
 *     Use a byte array as object and a Topic as destination.
 */
public class Test_T_MObjBA extends TestCase implements javax.jms.MessageListener  {
  public ObjectMessage msg_send; // store object  message sent by pruducer

  public static void main(String[] args) {
    new Test_T_MObjBA().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      admin();
      System.out.println("admin config ok");

      Context  ictx = new InitialContext();
      Topic topic = (Topic) ictx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false,
                                           Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false,
                                           Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(topic);
      MessageConsumer consumer = sessionc.createConsumer(topic);

      // the consumer records on the topic
      consumer.setMessageListener(this);

      // create an object and a message containing the object send to the topic by the pruducer
      byte[] obj_send = new byte[10];
      for (int i=0; i<10; i++) obj_send[i] = (byte) i;
      msg_send = sessionp.createObjectMessage();
      msg_send.setObject(obj_send);
      producer.send(msg_send);

      // Wait to receive the message.
      Thread.sleep(1000);
      cnx.close();
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
   * Admin : Create topic and a user anonymous
   *   use jndi
   */
  public void admin() throws Exception {
    // conexion 
    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
                                                             "root", "root", 60);
    // create a Topic   
    org.objectweb.joram.client.jms.Topic topic =
      (org.objectweb.joram.client.jms.Topic) org.objectweb.joram.client.jms.Topic.create("topic"); 

    // create a user
    org.objectweb.joram.client.jms.admin.User user =
      org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
    // set permissions
    topic.setFreeReading();
    topic.setFreeWriting();

    javax.jms.ConnectionFactory cf =
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }


  public void onMessage(Message message) {
    System.out.println("message receive");
    try{
      ObjectMessage msg = (ObjectMessage) message;
      // and extract the object
      byte[] obj_producer = (byte[]) msg_send.getObject();
      byte[] obj_consumer = (byte[]) msg.getObject();
      //test messages
      assertEquals(msg_send.getJMSMessageID(),msg.getJMSMessageID());
      assertEquals(msg_send.getJMSType(),msg.getJMSType());
      assertEquals(msg_send.getJMSDestination(),msg.getJMSDestination());
      assertEquals(obj_producer.length,obj_consumer.length);
      assertEquals(obj_producer,obj_consumer, obj_producer.length);
    }catch(javax.jms.JMSException JE){
      JE.printStackTrace( );
    }
  }
}

