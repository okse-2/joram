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
public class AliasTestQ2 extends TestCase {

  public static void main(String[] args) {
    new AliasTestQ2().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false" });
      startAgentServer((short) 2);

      admin();

      Context ictx = new InitialContext();
      Queue aliasQ = (Queue) ictx.lookup("queue0");
      Queue queue = (Queue) ictx.lookup("queue1");
      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf2 = (ConnectionFactory) ictx.lookup("cf2");
      ictx.close();

      Connection cnx0 = cf0.createConnection();
      Session sessionp = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx0.start();

      Connection cnx2 = cf2.createConnection();
      Session sessionc = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx2.start();
      MessageConsumer consumer = sessionc.createConsumer(queue);

      // create a producer
      MessageProducer producer = sessionp.createProducer(aliasQ);

      for (int i = 0; i < 10; i++) {
        TextMessage msg = sessionp.createTextMessage();
        msg.setText("Message n�" + i);
        producer.send(msg);
      }

      for (int i = 0; i < 10; i++) {
        Message msg = consumer.receive();
        assertEquals("Message n�" + i, ((TextMessage) msg).getText());
      }

      Thread.sleep(1000);
      
      // Kill server holding the final queue
      killAgentServer((short) 1);

      for (int i = 0; i < 10; i++) {
        TextMessage msg = sessionp.createTextMessage();
        msg.setText("Message n�" + i);
        producer.send(msg);
      }

      // Let some time for the notifications to expire in the network and go back in the alias queue
      Thread.sleep(5000);

      AdminModule.connect(cf0, "root", "root");

      System.out.println("count: " + aliasQ.getPendingMessages());
      assertEquals(10, aliasQ.getPendingMessages());
      AdminModule.disconnect();

      // Restart server
      startAgentServer((short) 1);

      // Alias queue should transmit the waiting messages to the final queue
      for (int i = 0; i < 10; i++) {
        Message msg = consumer.receive(10000);
        assertEquals("Message n�" + i, ((TextMessage) msg).getText());
      }

      cnx0.close();
      cnx2.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      endTest();
    }
  }

  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {

    AdminModule.connect("localhost", 16010, "root", "root", 60);

    // Creating access for user anonymous on servers 0 and 2:
    User.create("anonymous", "anonymous", 0);
    User.create("anonymous", "anonymous", 2);

    // Creating the destination on server 1:
    Queue queue1 = Queue.create(1);
    queue1.setFreeWriting();
    queue1.setFreeReading();

    // Create an alias queue on server 0 linking to queue1
    Properties props = new Properties();
    props.setProperty("remoteAgentID", queue1.getName());
    Queue queue0 = Queue.create(0, "org.objectweb.joram.mom.dest.AliasQueue", props);
    queue0.setFreeWriting();

    // Creating the connection factories for connecting to the servers 0 and 2:
    javax.jms.ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);
    javax.jms.ConnectionFactory cf2 = TcpConnectionFactory.create("localhost", 16012);

    // Binding the objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue0", queue0);
    jndiCtx.bind("queue1", queue1);
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
