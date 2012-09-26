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
import org.objectweb.joram.mom.dest.jms.JMSAcquisition;

import framework.TestCase;

/**
 * Test: Test the bridge behavior during stop / restart of Joram server.
 *  - Sends 50 messages on foreign server.
 *  - Stops the Joram server during sending, then restarts it.
 *  - Receives the messages through a JMS AcquisitionQueue.
 */
public class BridgeTest9x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest9x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

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

        // bind foreign destination and connectionFactory
        jndiCtx.rebind("foreignQueue", foreignQueue);
        jndiCtx.rebind("foreignCF", foreignCF);
      
        // Setting the bridge properties
        Properties prop = new Properties();
        prop.setProperty("jms.DestinationName", "foreignQueue");
        prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop.setProperty("period", "1000");
//        prop.setProperty("persistent", "true");
        prop.setProperty("acquisition.className", JMSAcquisition.class.getName());

        // Creating a Queue bridge on server 0:
        Queue joramInQueue = Queue.create(0, "BridgeInQueue", Queue.ACQUISITION_QUEUE, prop);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramCF", joramCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
      }

      System.out.println("admin config ok");
      Thread.sleep(1000);

      for (int i=0; i<20; i++)
        test(100);
      
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

  final static long timeout = 5L;
  
  void test(final int msgs) throws Exception {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");      
    jndiCtx.close();

    Connection foreignCnx = foreignCF.createConnection();
    Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer foreignProd = foreignSess.createProducer(foreignDest);
    foreignCnx.start();

    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep((2*timeout/5)*msgs);
          crashAgentServer((short) 0);
//          System.out.println("Joram server stopped.");
          startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
//          System.out.println("Joram server started.");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    
    TextMessage msgOut = foreignSess.createTextMessage();
    for (int i = 0; i < msgs; i++) {
      msgOut.setText("Message number " + i);
//      System.out.println("send msg = " + msgOut.getText());
      foreignProd.send(msgOut);
      Thread.sleep(timeout);
    }
    
    foreignCnx.close();

    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
    joramCnx.start(); 

    int nbmsg = 0;
    TextMessage msgIn;
    for (int i = 0; i < msgs; i++) { 
      msgIn=(TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        assertEquals("Message number " + i, msgIn.getText());
        nbmsg += 1;
      } else {
        assertTrue("Message not received", false);
        break;
      }
//      System.out.println("receive msg = " + msgIn.getText());
    }
    assertEquals(msgs, nbmsg);
    
    joramCnx.close();
  }
}

