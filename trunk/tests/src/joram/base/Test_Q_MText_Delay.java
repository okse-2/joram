/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test :
 *  send text message with delay
 *  receive the message after expiration of the delay
 *  Use a Queue
 */
public class Test_Q_MText_Delay extends TestCase {

  public static void main(String[] args) {
    new Test_Q_MText_Delay().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);
      Thread.sleep(2000L);
      admin();
      System.out.println("admin config ok");

      Context  ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      long deliveryDelay = 2000;
      // create a producer
      MessageProducer producer = sessionp.createProducer(queue);
      // set a delivery delay
      producer.setDeliveryDelay(deliveryDelay);
      // create a consumer
      MessageConsumer consumer = sessionc.createConsumer(queue);
      // create a text message send to the queue by the producer 
      TextMessage msg = sessionp.createTextMessage();
      msg.setText("message_text_with_delay");
      long current = System.currentTimeMillis();
      producer.send(msg);

      assertTrue("The JMSDeliveryTime must be > 0", msg.getJMSDeliveryTime() > 0);

      // the consumer receive the message from the queue
      msg = (TextMessage) consumer.receive(100);
      // the received message must be null
      assertEquals(null, msg);

      msg = (TextMessage) consumer.receive();
      //System.out.println("msg = " + msg);
      assertTrue("bad delivery delay", (System.currentTimeMillis() - current) > deliveryDelay);

      //test messages
      assertEquals(msg.getJMSMessageID(), msg.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg.getJMSType());
      assertEquals(msg.getJMSDestination(), msg.getJMSDestination());
      assertEquals("message_text_with_delay", msg.getText());
      assertTrue("bad JMSDeliveryTime must be > 0", msg.getJMSDeliveryTime() > 0);

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
 
  public void admin() throws Exception {
    // conexion 
    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    AdminModule.connect(cf, "root", "root");
    
    // create a Queue   
    Queue queue = org.objectweb.joram.client.jms.Queue.create("queue"); 
    // set permissions
    ((org.objectweb.joram.client.jms.Queue) queue).setFreeReading();
    ((org.objectweb.joram.client.jms.Queue) queue).setFreeWriting();
    
    // create a user
    User.create("anonymous", "anonymous");

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}

