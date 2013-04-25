/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2013 ScalAgent Distributed Technologies
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
package joram.bridgeamqp;

import java.io.File;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.AMQPAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.AMQPDistributionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Use 2 bridge queues to send and receive messages through a foreign AMQP queue.
 *       Test the bridge behavior with kill and start of the Joram bridge server.
 *  - Sends 10 messages.
 *  - Receives 10 messages.
 *  - Kills then restarts the bridge server.
 *  - Sends 10 messages.
 *  - Receives 10 messages.
 *  - Sends 10 messages.
 *  - Kills then restarts the bridge server.
 *  - Receives the waiting 10 messages.
 */
public class Test2 extends TestCase {

  public static void main(String[] args) {
    new Test2().run();
  }

  public void run() {
    Process s1 = null;
    try {
      System.out.println("servers start");
      // Starts Joram JMS server
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      // Starts Joram AMQP Server
      s1 = startProcess("fr.dyade.aaa.agent.AgentServer",
                                null,
                                new String[] { "-Dcom.sun.management.jmxremote" },
                                new String[] { "1", "./s1" },
                                new File("./amqp"));

      // Wait for the AMQP server starting
      Thread.sleep(2000);
      
      // Administration code
      
      {
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

        AdminModule.connect(joramCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);

        // Setting the bridge properties
        Properties prop1 = new Properties();
        prop1.setProperty("amqp.Queue.DeclarePassive", "false");
        prop1.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop1.setProperty("amqp.Queue.DeclareDurable", "true");
        prop1.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop1.setProperty("amqp.ConnectionUpdatePeriod", "1000");
        
        Queue joramInQueue = AMQPAcquisitionQueue.create(0, "queue", "amqpQueue", prop1);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        // Setting the bridge properties
        Properties prop2 = new Properties();
        prop2.setProperty("amqp.Queue.DeclarePassive", "false");
        prop2.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop2.setProperty("amqp.Queue.DeclareDurable", "true");
        prop2.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop2.setProperty("amqp.ConnectionUpdatePeriod", "1000");
        prop2.setProperty("period", "1000");      
        prop2.put("distribution.async", "" + async);

        Queue joramOutQueue = AMQPDistributionQueue.create(0, "BridgeOutQueue", "amqpQueue", prop2);
        joramOutQueue.setFreeWriting();
        System.out.println("joramOutQueue = " + joramOutQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramOutQueue", joramOutQueue);
        jndiCtx.rebind("joramCF", joramCF);

        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }

      System.out.println("admin config ok");
      Thread.sleep(1000);

      // Gets administered objects from JNDI
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
      Destination joramOutDest = (Destination) jndiCtx.lookup("joramOutQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();
      
      // Creates needed Connection and Session objects
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
      MessageProducer joramProd = joramSess.createProducer(joramOutDest);
      joramCnx.start(); 

      // Run the simple test: send 10 messages then receive it.
      TextMessage msgOut = joramSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        msgOut.setText("Message number " + i);
//        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
      }

      int nbmsg = 0;
      TextMessage msgIn;
      for (int i = 1; i < 11; i++) { 
        msgIn=(TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
//        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + i, msgIn.getText());
      }
      assertEquals(10, nbmsg);

      // TODO (AF): This sleep is needed to avoid message replication. This issue
      // needs to be study.
      Thread.sleep(500);
      System.out.println("Kills the bridge server.");
      killAgentServer((short) 0);
      Thread.sleep(1000);
      startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(1000);
      
      // Creates needed Connection and Session objects
      joramCnx = joramCF.createConnection();
      joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      joramCons = joramSess.createConsumer(joramInDest);
      joramProd = joramSess.createProducer(joramOutDest);
      joramCnx.start(); 

      // Run the simple test: send 10 messages then receive it.
      msgOut = joramSess.createTextMessage();
      for (int i = 11; i < 21; i++) {
        msgOut.setText("Message number " + i);
//        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
      }

      for (int i = 11; i < 21; i++) { 
        msgIn=(TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
//        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + i, msgIn.getText());
      }
      assertEquals(20, nbmsg);

      // Send 10 messages then receive it after server restart.
      msgOut = joramSess.createTextMessage();
      for (int i = 21; i < 31; i++) {
        msgOut.setText("Message number " + i);
//        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
      }

      System.out.println("Kills the bridge server.");
      killAgentServer((short) 0);
      Thread.sleep(1000);
      startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(1000);
      
      // Creates needed Connection and Session objects
      joramCnx = joramCF.createConnection();
      joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      joramCons = joramSess.createConsumer(joramInDest);
      joramProd = joramSess.createProducer(joramOutDest);
      joramCnx.start(); 

      // Receive the waiting messages.
      for (int i = 21; i < 31; i++) { 
        msgIn=(TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
//        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + i, msgIn.getText());
      }
      assertEquals(30, nbmsg);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      s1.destroy();
      killAgentServer((short)0);
      endTest(); 
    }
  }
}

