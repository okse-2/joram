/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.recovery;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Tests recovery with 2 servers and a durable subscriber.
 * <ul>
 * <li>Start 2 servers.</li>
 * <li>Send 2 messages.</li>
 * <li>Stop second server.</li>
 * <li>Send 2 messages.</li>
 * <li>Stop first server.</li>
 * <li>Restart the 2 servers and check reception of four messages.</li>
 * </ul>
 */
public class Test6 extends framework.TestCase {
  public static void main(String args[]) {
    new Test6().run();
  }

  public void run() {
    try {
      startAgentServer((short) 1);
      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();
      Thread.sleep(1000L);

      AdminModule.connect("root", "root", 60);
      Topic topic = Topic.create(1, "Topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      User.create("anonymous", "anonymous", 0);
      User.create("anonymous", "anonymous", 1);
      AdminModule.disconnect();

      ConnectionFactory cf0 = LocalConnectionFactory.create();
      ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 16011);

      Connection cnx = cf1.createConnection();
      cnx.setClientID("Test6");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session.createDurableSubscriber(topic, "subname");
      session.close();
      cnx.close();

      //System.out.println("Subscription done");

      cnx = cf0.createConnection();
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(topic);
      Message msg = session.createMessage();
      msg.setStringProperty("name", "msg#1");
      producer.send(msg);
      msg.setStringProperty("name", "msg#2");
      producer.send(msg);

      //System.out.println("Messages #1, #2 sent");

      Thread.sleep(1000L);
      stopAgentServer((short) 1);

      System.out.println("Server#1 stopped");

      msg.setStringProperty("name", "msg#3");
      producer.send(msg);
      msg.setStringProperty("name", "msg#4");
      producer.send(msg);
      session.close();
      cnx.close();

      //System.out.println("Messages #3, #4 sent");

      Thread.sleep(1000L);
      AgentServer.stop();
      AgentServer.reset();
      Thread.sleep(1000L);

      System.out.println("Server#0 stopped");

      startAgentServer((short) 1);

      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();
      Thread.sleep(2000L);

      System.out.println("Servers #0, #1 started");

      cnx = cf1.createConnection();
      cnx.setClientID("Test6");
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      consumer = session.createDurableSubscriber(topic, "subname");
      cnx.start();

      //System.out.println("before receive");
      msg = consumer.receive();
      //System.out.println("receives: " + msg.getStringProperty("name"));
      assertEquals("msg#1", msg.getStringProperty("name"));

      msg = consumer.receive();
      //System.out.println("receives: " + msg.getStringProperty("name"));
      assertEquals("msg#2", msg.getStringProperty("name"));

      msg = consumer.receive();
      //System.out.println("receives: " + msg.getStringProperty("name"));
      assertEquals("msg#3", msg.getStringProperty("name"));

      msg = consumer.receive();
      //System.out.println("receives: " + msg.getStringProperty("name"));
      assertEquals("msg#4", msg.getStringProperty("name"));

      session.close();

      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      session.unsubscribe("subname");
      session.close();
      cnx.close();

      Thread.sleep(1000L);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 1);
      AgentServer.stop();
      endTest();
    }

  }
}
