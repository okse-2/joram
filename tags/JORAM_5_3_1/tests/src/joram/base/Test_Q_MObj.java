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

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import framework.TestCase;


/**
 * Test :
 *     The Object message received by the consumer is the same that 
 *     the object message sent by the producer 
 *     Use String as object and a Queue as destination.
 */
public class Test_Q_MObj extends TestCase {
  public static void main(String[] args) {
    new Test_Q_MObj().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      admin();
      System.out.println("admin config ok");

      Context  ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // create a producer
      MessageProducer producer = session.createProducer(queue);
      // start the session
      cnx.start();

      // create an object and send the message containing the object to the queue 
      Serializable obj_send = new String("abcde");
      ObjectMessage msg = session.createObjectMessage();
      msg.setObject(obj_send);
      producer.send(msg);
      
      producer.close();
      session.close();
      cnx.close();

      cnx = cf.createConnection();
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // create a consumer
      MessageConsumer consumer = session.createConsumer(queue);
      // start the session
      cnx.start();

      // the consumer receive the first message from the queue
      ObjectMessage msg2 = (ObjectMessage) consumer.receive();
      // and extract the object
      String obj_receive = (String) msg2.getObject();

      //test messages
      assertEquals(msg.getJMSMessageID(), msg2.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg2.getJMSType());
      assertEquals(msg.getJMSDestination(), msg2.getJMSDestination());
      assertEquals(obj_send, obj_receive);
      
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
   * Admin : Create queue and a user anonymous
   *   use jndi
   */
  public void admin() throws Exception {
    // conexion 
    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
                                                             "root", "root", 60);
    // create a Queue   
    org.objectweb.joram.client.jms.Queue queue =
      (org.objectweb.joram.client.jms.Queue) org.objectweb.joram.client.jms.Queue.create("queue"); 

    // create a user
    org.objectweb.joram.client.jms.admin.User user =
      org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    javax.jms.ConnectionFactory cf =
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}


