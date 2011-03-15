/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2009 ScalAgent Distributed Technologies
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
 * Test transacted. Use a queue. Make lots of receiveNoWait before committing.
 */
public class Test_Trans_Q2 extends TestCase {

  public static void main(String[] args) {
    new Test_Trans_Q2().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);
      Thread.sleep(2000);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(queue);
      MessageConsumer consumer = sessionc.createConsumer(queue);
      
      for (int i = 0; i < 100000; i++) {
        consumer.receiveNoWait();
      }

      // create a message send to the queue by the producer 
      Message msg = sessionp.createMessage();

      producer.send(msg);

      Message msg1 = consumer.receive(3000);
      // sessionp not commit, so the message is not delivered
      assertEquals(null, msg1);

      // commit
      sessionp.commit();

      // receive the message
      msg1 = consumer.receive(3000);
      assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg1.getJMSType());
      assertEquals(msg.getJMSDestination(), msg1.getJMSDestination());

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
    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
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
