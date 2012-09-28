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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test the bridge behavior during stop / restart of Foreign server.
 *  - Sends 1000 messages on foreign server.
 *  - Stops the Joram server during sending, then restarts it.
 *  - Receives the messages through a JMS AcquisitionQueue.
 */
public class BridgeTest13x extends TestCase implements MessageListener {
  public static void main(String[] args) {
    new BridgeTest13x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
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
        prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop.setProperty("period", "1000");
//        prop.setProperty("acquisition.max_msg", "5000");
//        prop.setProperty("acquisition.max_pnd", "5000");
        // Creating a Queue bridge on server 0:
        Queue joramInQueue = JMSAcquisitionQueue.create(0, "joramInQueue", "foreignQueue", prop);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramCF", joramCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
        exc.printStackTrace();
      }

      System.out.println("admin config ok");
      Thread.sleep(1000);

      for (int i=0; i<10; i++)
        if (! test(1000)) break;
            
      try {
        Thread.sleep(100);
        killAgentServer((short) 0);
        System.out.println("Joram server stopped.");
        startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
        //          System.out.println("Foreign server started.");
      } catch (Exception e) {
        e.printStackTrace();
      }
      Thread.sleep(2000);

      test(1000);
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

  final static long timeout = 10L;
  
  boolean test(final int msgs) throws Exception {
    System.out.println("test start..");
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");      
    jndiCtx.close();

    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
    joramCons.setMessageListener(this);
    joramCnx.start(); 

    Connection foreignCnx = foreignCF.createConnection();
    Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer foreignProd = foreignSess.createProducer(foreignDest);
    foreignCnx.start();

    nbmsg = 0;
    TextMessage msgOut = foreignSess.createTextMessage();
    for (int i = 0; i < msgs; i++) {
      msgOut.setText("Message number " + i);
      //      System.out.println("send msg = " + msgOut.getText());
      foreignProd.send(msgOut);
      //      Thread.sleep(timeout);
    }
    foreignCnx.close();

    try {
      Thread.sleep(100);
      killAgentServer((short) 1);
      System.out.println("Foreign server stopped: " + nbmsg);
      startAgentServer((short) 1, new String[]{"-DTransaction.UseLockFile=false"});
      //          System.out.println("Foreign server started.");
    } catch (Exception e) {
      e.printStackTrace();
    }    

    Thread.sleep((timeout*msgs) +5000L);
    System.out.println("Receives " + nbmsg + " messages.");
    assertEquals("Receives " + nbmsg + "messages, should be " + msgs, msgs, nbmsg);

    joramCnx.close();
    
    return (nbmsg == msgs);
  }
  
  int nbmsg = 0;
  public void onMessage(Message msg) {
//    System.out.println("receives: " + msg);
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e1) {}
    try {
      String txt1 = "Message number " + nbmsg;
      String txt2 = ((TextMessage) msg).getText();
      if (! txt1.equals(txt2))
        System.out.println("Expected <" + txt1 + "> but was <" + txt2 + "> ");
      assertEquals(txt1, txt2);
    } catch (JMSException e) {
      assertTrue("Exception: " + e, false);
      e.printStackTrace();
    }
    nbmsg += 1;
  }
}

