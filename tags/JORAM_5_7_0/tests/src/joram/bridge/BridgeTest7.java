/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
package joram.bridge;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;

import framework.TestCase;


/**
 * Test : One bridge to multiple JMS servers with round robin distribution.
 */
public class BridgeTest7 extends TestCase {

  public static void main(String[] args) {
    new BridgeTest7().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      startAgentServer((short) 2);

      Thread.sleep(8000);
      admin();
      Thread.sleep(1000);

      System.out.println("admin config ok");

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      Destination joramDest = (Destination) jndiCtx.lookup("joramQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();

      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      props.setProperty(Context.PROVIDER_URL, "scn://localhost:16401");
      jndiCtx = new javax.naming.InitialContext(props);
      Destination foreignDest1 = (Destination) jndiCtx.lookup("foreignQueue");
      ConnectionFactory cfS1 = (ConnectionFactory) jndiCtx.lookup("cfS1");
      jndiCtx.close();

      props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      props.setProperty(Context.PROVIDER_URL, "scn://localhost:16402");
      jndiCtx = new javax.naming.InitialContext(props);
      Destination foreignDest2 = (Destination) jndiCtx.lookup("foreignQueue");
      ConnectionFactory cfS2 = (ConnectionFactory) jndiCtx.lookup("cfS2");
      jndiCtx.close();

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer joramSender = joramSess.createProducer(joramDest);

      Connection foreignCnx1 = cfS1.createConnection();
      Session foreignSess1 = foreignCnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer foreignCons1 = foreignSess1.createConsumer(foreignDest1);

      Connection foreignCnx2 = cfS2.createConnection();
      Session foreignSess2 = foreignCnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer foreignCons2 = foreignSess2.createConsumer(foreignDest2);

      foreignCnx1.start();
      foreignCnx2.start();
      joramCnx.start();

      TextMessage msg = joramSess.createTextMessage();

      for (int i = 1; i < 11; i++) {
        msg.setText("Joram message number " + i);
        System.out.println("send msg = " + msg.getText());
        joramSender.send(msg);
      }

      for (int i = 1; i < 6; i++) {
        msg = (TextMessage) foreignCons1.receive(5000);
        assertNotNull(msg);
        System.out.println("Consumer 1: receive msg = " + msg.getText());
      }

      for (int i = 1; i < 6; i++) {
        msg = (TextMessage) foreignCons2.receive(5000);
        assertNotNull(msg);
        System.out.println("Consumer 2: receive msg = " + msg.getText());
      }

      foreignCnx1.close();
      foreignCnx2.close();
      joramCnx.close();
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

  void admin() throws Exception {
    AdminModule.connect("root", "root", 60);
    
    User.create("anonymous", "anonymous", 0);
    User.create("anonymous", "anonymous", 1);
    User.create("anonymous", "anonymous", 2);

    // Configure first foreign server
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    props.setProperty(Context.PROVIDER_URL, "scn://localhost:16401");
    javax.naming.Context jndiCtx = new javax.naming.InitialContext(props);
    
    Queue foreignQueue1 = Queue.create(1, "foreignQueue");
    foreignQueue1.setFreeReading();
    foreignQueue1.setFreeWriting();
    System.out.println("foreign queue = " + foreignQueue1);

    javax.jms.ConnectionFactory cfS1 = TcpConnectionFactory.create("localhost", 16011);

    jndiCtx.rebind("foreignQueue", foreignQueue1);
    jndiCtx.rebind("cfS1", cfS1);
    jndiCtx.close();

    // Configure second foreign server
    props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    props.setProperty(Context.PROVIDER_URL, "scn://localhost:16402");
    jndiCtx = new javax.naming.InitialContext(props);

    Queue foreignQueue2 = Queue.create(2, "foreignQueue");
    foreignQueue2.setFreeReading();
    foreignQueue2.setFreeWriting();
    System.out.println("foreign queue = " + foreignQueue2);

    javax.jms.ConnectionFactory cfS2 = TcpConnectionFactory.create("localhost", 16012);

    jndiCtx.rebind("foreignQueue", foreignQueue2);
    jndiCtx.rebind("cfS2", cfS2);
    jndiCtx.close();

    // Configure the distribution server
    Properties prop = new Properties();
    jndiCtx = new javax.naming.InitialContext();

    prop.setProperty("jms.DestinationName", "foreignQueue");
    prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
//    prop.setProperty("period", "1000");
    prop.setProperty("distribution.className", JMSDistribution.class.getName());

    Queue joramQueue = Queue.create(0, Queue.DISTRIBUTION_QUEUE, prop);
    joramQueue.setFreeWriting();
    System.out.println("joram queue = " + joramQueue);

    javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();

    jndiCtx.rebind("joramQueue", joramQueue);
    jndiCtx.rebind("joramCF", joramCF);

    jndiCtx.close();

    // Add the 2 foreign server JMS connections
    AdminModule.invokeStaticServerMethod("org.objectweb.joram.mom.dest.jms.JMSConnectionService",
        "addServer", new Class[] { String.class, String.class, String.class }, new Object[] { "cfS1",
            "fr.dyade.aaa.jndi2.client.NamingContextFactory", "scn://localhost:16401" });

    AdminModule.invokeStaticServerMethod("org.objectweb.joram.mom.dest.jms.JMSConnectionService",
        "addServer", new Class[] { String.class, String.class, String.class }, new Object[] { "cfS2",
            "fr.dyade.aaa.jndi2.client.NamingContextFactory", "scn://localhost:16402" });

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}

