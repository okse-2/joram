/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 ScalAgent Distributed Technologies
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

import framework.TestCase;

/**
 * Test: Use 2 bridge queues to send and receive a message through a foreign queue.
 *  - sends 1 message then receive it
 *  - stops then restarts the foreign server
 *  - try to receive anew the message
 */
public class BridgeTestX3 extends TestCase {
  public static void main(String[] args) {
    new BridgeTestX3().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        AdminModule.connect("root", "root", 60);
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);
        User.create("anonymous", "anonymous", 1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue = Queue.create(1, "foreignQueue");
        foreignQueue.setFreeReading();
        foreignQueue.setFreeWriting();
        System.out.println("foreign queue = " + foreignQueue);

        javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

        // bind foreign destination and connectionFactory
        jndiCtx.rebind("foreignQueue", foreignQueue);
        jndiCtx.rebind("foreignCF", foreignCF);

        // Setting the bridge properties
        Properties prop = new Properties();
        // Foreign QueueConnectionFactory JNDI name: foreignCF
        prop.setProperty("connectionFactoryName", "foreignCF");
        // Foreign Queue JNDI name: foreignDest
        prop.setProperty("destinationName", "foreignQueue");
        // automaticRequest
        prop.setProperty("automaticRequest", "true");

        // Creating a Queue "IN" bridge on server 0:
        Queue joramInQueue = Queue.create(0, "BridgeInQueue",
                                          "org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeQueue",
                                          prop);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        // Setting the bridge properties
        prop = new Properties();
        // Foreign QueueConnectionFactory JNDI name: foreignCF
        prop.setProperty("connectionFactoryName", "foreignCF");
        // Foreign Queue JNDI name: foreignDest
        prop.setProperty("destinationName", "foreignQueue");
        // automaticRequest
        prop.setProperty("automaticRequest", "false");

        // Creating a Queue "OUT" bridge on server 0:
        Queue joramOutQueue = Queue.create(0, "BridgeOutQueue",
                                           "org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeQueue",
                                           prop);
        joramOutQueue.setFreeReading();
        joramOutQueue.setFreeWriting();
        System.out.println("joramOutQueue = " + joramOutQueue);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramOutQueue", joramOutQueue);
        jndiCtx.rebind("joramCF", joramCF);

        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
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
      msgOut.setText("Message number " + 1);
      System.out.println("send msg = " + msgOut.getText());
      joramProd.send(msgOut);

      TextMessage msgIn;
      msgIn=(TextMessage) joramCons.receive(5000);
      if (msgIn == null) {
        assertTrue("Message not received", false);
      } else {
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + 1, msgIn.getText());
      }

      Thread.sleep(2000L); // Wait for acknowledge avoiding to deliver anew the message
      stopAgentServer((short)1);
      System.out.println("Bridge server stopped.");
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(5000);

      msgIn=(TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        System.out.println("receive msg = " + msgIn.getText());
        assertTrue("Message received", false);
      }

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

