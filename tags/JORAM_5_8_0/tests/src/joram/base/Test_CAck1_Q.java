/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2009 ScalAgent Distributed Technologies
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
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test Client_ackowledge. If no acknowledgement, the message is sent again.
 * When use manual acknowledge the message is deleting Use a queue
 */
public class Test_CAck1_Q extends TestCase {

  public static void main(String[] args) {
    new Test_CAck1_Q().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");

      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();

      Session sessionp = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(queue);
      MessageConsumer consumer = sessionc.createConsumer(queue);

      // create a message send to the queue by the producer 
      Message msg = sessionp.createMessage();

      producer.send(msg);

      // receive the message
      Message msg1 = consumer.receive(10000);
      assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg1.getJMSType());
      assertEquals(msg.getJMSDestination(), msg1.getJMSDestination());

      // session close without acknowledge
      sessionc.close();

      // reconnection
      sessionc = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      consumer = sessionc.createConsumer(queue);
      // message is redelivered
      msg1 = consumer.receive(10000);
      //test
      assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg1.getJMSType());
      assertEquals(msg.getJMSDestination(), msg1.getJMSDestination());

      // acknowledge
      msg1.acknowledge();

      sessionc.close();
      sessionc = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      consumer = sessionc.createConsumer(queue);
      
      // message is not redelivered 
      msg1 = consumer.receive(5000);
      assertEquals(null, msg1);
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
    // connection 
    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().connectingTimer = 10;
    AdminModule.connect(cf);

    // create a Queue   
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(0);
    // create a user
    User.create("anonymous", "anonymous");

    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    Context jndiCtx = new InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);

    jndiCtx.close();

    AdminModule.disconnect();
  }
}
