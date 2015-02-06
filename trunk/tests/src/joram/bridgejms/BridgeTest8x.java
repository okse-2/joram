/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 - 2015 ScalAgent Distributed Technologies
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
package joram.bridgejms;

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
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSAcquisition;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminReply;

import framework.TestCase;

/**
 * Test: Use 2 bridge queues to send and receive messages through a foreign queue.
 *  - Sends 10 messages.
 *  - Receives 10 messages.
 *  - Stops the foreign server.
 *  - Sends 60 messages.
 *  - Starts the foreign server.
 *  - Receives the messages sent.
 *  - Sends 10 more messages and receives it.
 *  - Stops the handler,
 *  - Sends 10 messages,
 *  - Verify that there is no message Joram side,
 *  - Restarts the handler and receives the waiting messages.
 */
public class BridgeTest8x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest8x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      {
        // Administration code
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);
        ((org.objectweb.joram.client.jms.ConnectionFactory) joramCF).getParameters().connectingTimer = 0;

        AdminModule.connect(joramCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);
        User.create("anonymous", "anonymous", 1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue = Queue.create(1, "foreignQueue");
        foreignQueue.setFreeReading();
        foreignQueue.setFreeWriting();
        System.out.println("foreign queue = " + foreignQueue);

        javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
        ((org.objectweb.joram.client.jms.ConnectionFactory) foreignCF).getParameters().connectingTimer = 0;

        // bind foreign destination and connectionFactory
        jndiCtx.rebind("foreignQueue", foreignQueue);
        jndiCtx.rebind("foreignCF", foreignCF);

        // Setting the bridge properties
        Properties prop1 = new Properties();
        prop1.setProperty("jms.DestinationName", "foreignQueue");
        prop1.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop1.setProperty("period", "1000");      
        prop1.setProperty("acquisition.className", JMSAcquisition.class.getName());

        // Creating a Queue bridge on server 0:
        Queue joramInQueue = Queue.create(0, "BridgeInQueue", Queue.ACQUISITION_QUEUE, prop1);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        // Setting the bridge properties
        Properties prop2 = new Properties();
        prop2.setProperty("jms.DestinationName", "foreignQueue");
        prop2.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop2.setProperty("period", "1000");      
        prop2.put("distribution.async", "" + async);
        prop2.setProperty("distribution.className", JMSDistribution.class.getName());

        Queue joramOutQueue = Queue.create(0, "BridgeOutQueue", Queue.DISTRIBUTION_QUEUE, prop2);
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
      Thread.sleep(5000);

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
      Destination joramOutDest = (Destination) jndiCtx.lookup("joramOutQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
      MessageProducer joramProd = joramSess.createProducer(joramOutDest);
      joramCnx.start(); 

      TextMessage msgOut = joramSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        msgOut.setText("Message number " + i);
        System.out.println("send msg = " + msgOut.getText());
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
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + i, msgIn.getText());
      }
      assertEquals(10, nbmsg);

      stopAgentServer((short)1);
      System.out.println("Foreign server stopped.");
      Thread.sleep(5000);

      int nbMsgDuringStop = 60;
      msgOut = joramSess.createTextMessage();
      for (int i = 0; i < nbMsgDuringStop; i++) {
        msgOut.setText("Message sent during stop, number " + i);
        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
        Thread.sleep(100);
      }

      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Foreign server started.");
      Thread.sleep(5000);

      for (int i = 0; i < nbMsgDuringStop; i++) { 
        msgIn=(TextMessage) joramCons.receive(60000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message sent during stop, number " + i, msgIn.getText());
      }
      assertEquals(nbMsgDuringStop+10, nbmsg);

      Thread.sleep(5000);

      msgOut = joramSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        msgOut.setText("Message sent after start, number " + i);
        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
      }

      //      killAgentServer((short)0);
      //      System.out.println("Joram server stopped.");
      //      startAgentServer((short)0, new File("."), new String[]{"-DTransaction.UseLockFile=false"});
      //      System.out.println("Joram server started.");
      //      Thread.sleep(10000);
      //
      //      joramCnx = joramCF.createConnection();
      //      joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      //      joramCons = joramSess.createConsumer(joramDest);
      //      joramCnx.start(); 

      for (int i = 1; i < 11; i++) { 
        msgIn=(TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message sent after start, number " + i, msgIn.getText());
      }
      assertEquals(nbMsgDuringStop+20, nbmsg);

      jndiCtx = new javax.naming.InitialContext();  
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
      jndiCtx.close();

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer foreignProd = foreignSess.createProducer(foreignDest);
      foreignCnx.start(); 

      TextMessage foreignMsg = foreignSess.createTextMessage();
      foreignMsg.setText("Message directly sent by foreign server");
      foreignProd.send(foreignMsg);

      msgIn= (TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message directly sent by foreign server", msgIn.getText());
      } else {
        assertTrue("Message not received", false);
      }

      AdminModule.connect(joramCF, "root", "root");
      AdminReply reply = AdminModule.processAdmin(((Queue) joramInDest).getName(),
                                                  AdminCommandConstant.CMD_STOP_HANDLER, null);
      System.out.println(reply);
      AdminModule.disconnect();

      foreignMsg = foreignSess.createTextMessage();
      foreignMsg.setText("2nd Message directly sent by foreign server");
      foreignProd.send(foreignMsg);

      msgIn= (TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        System.out.println("receive msg (should not) = " + msgIn.getText());
        assertEquals("2nd Message directly sent by foreign server", msgIn.getText());
        assertTrue("Message received", false);
      } else {
        assertTrue("Message not received", true);
      }

      AdminModule.connect(joramCF, "root", "root");
      reply = AdminModule.processAdmin(((Queue) joramInDest).getName(),
                                       AdminCommandConstant.CMD_START_HANDLER, null);
      System.out.println(reply);
      AdminModule.disconnect();

      msgIn= (TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("2nd Message directly sent by foreign server", msgIn.getText());
      } else {
        assertTrue("Message not received", true);
      }

      foreignCnx.close();
      joramCnx.close();      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      killAgentServer((short)1);
      endTest(); 
    }
  }

}

